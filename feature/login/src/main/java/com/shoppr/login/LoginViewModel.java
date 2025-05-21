package com.shoppr.login;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.shoppr.domain.usecase.CreateUserProfileUseCase;
import com.shoppr.domain.usecase.ObserveAuthStateUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends AndroidViewModel {
	private static final String TAG = "LoginViewModel";

	private final ObserveAuthStateUseCase observeAuthStateUseCase;
	// private final HandleSignInResultUseCase handleSignInResultUseCase; // Removed
	private final CreateUserProfileUseCase createUserProfileUseCase;

	// This LiveData from ObserveAuthStateUseCase gives the basic User from auth provider
	public final LiveData<User> rawAuthUserLiveData;

	// This will hold the full User object after profile creation/retrieval by CreateUserProfileUseCase
	private final MutableLiveData<User> _loggedInUserWithProfileLiveData = new MutableLiveData<>();
	public LiveData<User> loggedInUserWithProfileLiveData = _loggedInUserWithProfileLiveData;

	// For errors from CreateUserProfileUseCase or general auth failures post-UI flow
	private final MutableLiveData<Event<String>> _operationErrorEvents = new MutableLiveData<>();

	public LiveData<Event<String>> getOperationErrorEvents() { // Renamed for clarity
		return _operationErrorEvents;
	}

	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();

	public LiveData<Event<NavigationRoute>> getNavigationCommand() {
		return _navigationCommand;
	}

	// For toasts related to the immediate FirebaseUI flow (e.g. explicit user cancellation from IdpResponse)
	// This might become less used if we fully rely on ObserveAuthStateUseCase.
	private final MutableLiveData<Event<String>> _signInFlowFeedbackMessage = new MutableLiveData<>();

	public LiveData<Event<String>> getSignInFlowFeedbackMessage() {
		return _signInFlowFeedbackMessage;
	}

	// Flag to indicate that the FirebaseUI flow has returned, and we are now
	// actively waiting for ObserveAuthStateUseCase to provide a raw User to trigger profile creation.
	private boolean waitingForRawUserAfterAuthAttempt = false;
	private Observer<User> oneTimeRawAuthObserver;


	@Inject
	public LoginViewModel(@NonNull Application application,
												ObserveAuthStateUseCase observeAuthStateUseCase,
												CreateUserProfileUseCase createUserProfileUseCase) {
		super(application);
		this.observeAuthStateUseCase = observeAuthStateUseCase;
		this.createUserProfileUseCase = createUserProfileUseCase;
		this.rawAuthUserLiveData = this.observeAuthStateUseCase.getRawAuthUser();
	}

	// This is the observer that will react to the raw auth state AFTER a sign-in attempt.
	private void verifyRawUserAndCreateProfile(User rawUser) {
		Log.d(TAG, "verifyRawUserAndCreateProfile: Raw User: " + (rawUser != null ? rawUser.getId() : "null") + ", waitingForRawUser: " + waitingForRawUserAfterAuthAttempt);

		if (waitingForRawUserAfterAuthAttempt) {
			removeOneTimeRawAuthObserver(); // Critical: observe only once per attempt

			if (rawUser == null) {
				// Raw user is null even after FirebaseUI returned. This means auth failed.
				Log.w(TAG, "LoginViewModel: Raw auth user is NULL after sign-in attempt. Authentication failed.");
				_signInFlowFeedbackMessage.postValue(new Event<>("Authentication failed. Please try again."));
				_loggedInUserWithProfileLiveData.postValue(null);
				waitingForRawUserAfterAuthAttempt = false; // Reset flag
				return;
			}

			Log.d(TAG, "LoginViewModel: Raw auth user is available (UID: " + rawUser.getId() + "). Calling CreateUserProfileUseCase.");
			createUserProfileUseCase.execute(
					rawUser.getId(), rawUser.getName(), rawUser.getEmail(), null, // Pass basic info
					new CreateUserProfileUseCase.ProfileCreationCallbacks() {
						@Override
						public void onProfileReadyOrExists(User fullUserProfile) {
							Log.d(TAG, "LoginViewModel: CreateUserProfileUseCase success. Full profile for: " + fullUserProfile.getId());
							_loggedInUserWithProfileLiveData.postValue(fullUserProfile);
							_navigationCommand.postValue(new Event<>(new NavigationRoute.LoginToMap()));
							waitingForRawUserAfterAuthAttempt = false; // Reset flag
						}

						@Override
						public void onProfileCreationError(String message) {
							Log.e(TAG, "LoginViewModel: CreateUserProfileUseCase error: " + message);
							_operationErrorEvents.postValue(new Event<>(message));
							_loggedInUserWithProfileLiveData.postValue(null);
							waitingForRawUserAfterAuthAttempt = false; // Reset flag
						}
					}
			);
		}
	}

	private void addOneTimeRawAuthObserver() {
		removeOneTimeRawAuthObserver();

		oneTimeRawAuthObserver = this::verifyRawUserAndCreateProfile;
		this.rawAuthUserLiveData.observeForever(oneTimeRawAuthObserver);
		Log.d(TAG, "LoginViewModel: Added oneTimeRawAuthObserver to rawAuthUserLiveData.");
	}

	private void removeOneTimeRawAuthObserver() {
		if (oneTimeRawAuthObserver != null) {
			this.rawAuthUserLiveData.removeObserver(oneTimeRawAuthObserver);
			Log.d(TAG, "LoginViewModel: Removed oneTimeRawAuthObserver from rawAuthUserLiveData.");
		}
	}

	public void registerGlobalAuthStateObserver() {
		Log.d(TAG, "LoginViewModel: Telling ObserveAuthStateUseCase to start observing (globally).");
		observeAuthStateUseCase.startObserving();
	}

	public void unregisterGlobalAuthStateObserver() {
		Log.d(TAG, "LoginViewModel: Telling ObserveAuthStateUseCase to stop observing (globally).");
		observeAuthStateUseCase.stopObserving();
		removeOneTimeRawAuthObserver(); // Clean up if fragment stops while waiting
		waitingForRawUserAfterAuthAttempt = false; // Reset waiting state
	}

	public void processSignInResult(FirebaseAuthUIAuthenticationResult firebaseResult) {
		Log.d(TAG, "LoginViewModel: processSignInResult from Fragment. ResultCode: " + firebaseResult.getResultCode());
		waitingForRawUserAfterAuthAttempt = true; // Mark that an attempt has been made.
		addOneTimeRawAuthObserver(); // Start listening for the raw auth user.

		IdpResponse response = firebaseResult.getIdpResponse();

		if (response == null) {
			Log.d("LoginViewModel", "LoginViewModel: FirebaseUI flow returned null response.");
			waitingForRawUserAfterAuthAttempt = false; // UI flow error, stop waiting.
			removeOneTimeRawAuthObserver();
			return;
		}

		if (firebaseResult.getResultCode() != Activity.RESULT_OK) {
			waitingForRawUserAfterAuthAttempt = false; // User explicitly backed out of UI, stop waiting.
			removeOneTimeRawAuthObserver();
		}

		if (response.getError() != null) {
			Log.e(TAG, "LoginViewModel: FirebaseUI flow returned an error: " + response.getError().getMessage());
			_signInFlowFeedbackMessage.setValue(new Event<>("Sign-in error: " + response.getError().getMessage()));
			waitingForRawUserAfterAuthAttempt = false; // UI flow error, stop waiting.
			removeOneTimeRawAuthObserver();
		}
	}

	public void onNavigationComplete() {
		Log.d(TAG, "onNavigationComplete: Resetting _navigationCommand LiveData to null.");
		_navigationCommand.setValue(null);
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		Log.d(TAG, "onCleared: LoginViewModel is being cleared.");
		removeOneTimeRawAuthObserver();
	}
}