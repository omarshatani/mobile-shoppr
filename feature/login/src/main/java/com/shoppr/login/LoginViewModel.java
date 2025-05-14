package com.shoppr.login;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.shoppr.domain.HandleSignInResultUseCase;
import com.shoppr.domain.ObserveAuthStateUseCase;
import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;
import com.shoppr.ui.utils.Event;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends AndroidViewModel implements ObserveAuthStateUseCase.AuthCallbacks {
	private static final String TAG = "LoginViewModel";

	private final ObserveAuthStateUseCase observeAuthStateUseCase;
	private final HandleSignInResultUseCase handleSignInResultUseCase;

	public final LiveData<User> loggedInUserLiveData;

	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();

	public LiveData<Event<NavigationRoute>> getNavigationCommand() {
		return _navigationCommand;
	}

	private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();

	public LiveData<Event<String>> getToastMessage() {
		return _toastMessage;
	}

	@Inject
	public LoginViewModel(@NonNull Application application,
												ObserveAuthStateUseCase observeAuthStateUseCase,
												HandleSignInResultUseCase handleSignInResultUseCase) {
		super(application);
		this.observeAuthStateUseCase = observeAuthStateUseCase;
		this.handleSignInResultUseCase = handleSignInResultUseCase;

		this.observeAuthStateUseCase.setAuthCallbacks(this);
		this.loggedInUserLiveData = this.observeAuthStateUseCase.getLoggedInUser();
	}

	@Override
	public void onUserAuthenticatedAndProfileReady(User user) { // Route parameter removed
		Log.d(TAG, "LoginViewModel: onUserAuthenticatedAndProfileReady. User: " + user.getId());
		// This ViewModel knows that after its login flow, it should go to Map.
		_navigationCommand.postValue(new Event<>(new NavigationRoute.LoginToMap()));
	}

	@Override
	public void onAuthenticationError(String message) {
		Log.e(TAG, "LoginViewModel: onAuthenticationError. Message: " + message);
		_toastMessage.postValue(new Event<>(message));
	}

	@Override
	public void onUserLoggedOut() {
		Log.d(TAG, "LoginViewModel: onUserLoggedOut. User is now null.");
		// LoginViewModel is for the login screen. When onUserLoggedOut is called (e.g.,
		// if an auto-login attempt fails or user logs out from elsewhere while this VM is active),
		// it means the app should be on/remain on the login screen.
		// No navigation command needed from here to go *to* login, as this VM serves the login screen.
	}


	public void registerAuthStateListener() {
		Log.d(TAG, "LoginViewModel: Telling ObserveAuthStateUseCase to start observing.");
		observeAuthStateUseCase.startObserving();
	}

	public void unregisterAuthStateListener() {
		Log.d(TAG, "LoginViewModel: Telling ObserveAuthStateUseCase to stop observing.");
		observeAuthStateUseCase.stopObserving();
	}

	public void processSignInResult(FirebaseAuthUIAuthenticationResult firebaseResult) {
		Log.d(TAG, "LoginViewModel: processSignInResult from Fragment. ResultCode: " + firebaseResult.getResultCode());
		boolean isSuccess = firebaseResult.getResultCode() == Activity.RESULT_OK;
		IdpResponse idpResponse = firebaseResult.getIdpResponse();
		boolean isCancellation = false;
		String errorMessage = null;

		if (!isSuccess) {
			if (idpResponse == null) {
				isCancellation = true;
			} else if (idpResponse.getError() != null) {
				errorMessage = idpResponse.getError().getMessage();
			} else {
				errorMessage = "Sign-in flow didn't complete successfully. Code: " + firebaseResult.getResultCode();
			}
		}

		handleSignInResultUseCase.process(isSuccess, isCancellation, errorMessage, new HandleSignInResultUseCase.SignInResultCallbacks() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "LoginViewModel: HandleSignInResultUseCase reported SUCCESS from FirebaseUI flow.");
				// ObserveAuthStateUseCase will now take over, check profile, and trigger
				// onUserAuthenticatedAndProfileReady which posts the navigation event.
			}

			@Override
			public void onCancelled() {
				Log.w(TAG, "LoginViewModel: HandleSignInResultUseCase reported CANCELLED from FirebaseUI flow.");
				_toastMessage.setValue(new Event<>("Sign-in cancelled by user."));
			}

			@Override
			public void onError(String message) {
				Log.e(TAG, "LoginViewModel: HandleSignInResultUseCase reported ERROR from FirebaseUI flow: " + message);
				_toastMessage.setValue(new Event<>("Sign-in failed: " + message));
			}
		});
	}

	public void onNavigationEventHandled() {
		Log.d(TAG, "LoginViewModel: Navigation event considered handled by Fragment.");
	}

	public void onToastMessageEventHandled() {
		Log.d(TAG, "LoginViewModel: Toast message event considered handled by Fragment.");
	}

	@Override
	protected void onCleared() {
		super.onCleared();
	}
}
