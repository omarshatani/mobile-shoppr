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
import com.shoppr.domain.repository.AuthenticationRepository;
import com.shoppr.domain.repository.UserRepository;
import com.shoppr.model.Event;
import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends AndroidViewModel {
	private static final String TAG = "LoginViewModel";

	private final AuthenticationRepository authenticationRepository;
	private final UserRepository userRepository;

	public final LiveData<User> authState;

	private final MutableLiveData<User> _loggedInUserWithProfile = new MutableLiveData<>();
	public LiveData<User> loggedInUserWithProfileLiveData = _loggedInUserWithProfile;

	private final MutableLiveData<Event<String>> _operationErrorEvents = new MutableLiveData<>();

	public LiveData<Event<String>> getOperationErrorEvents() {
		return _operationErrorEvents;
	}

	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();
	public LiveData<Event<NavigationRoute>> getNavigationCommand() {
		return _navigationCommand;
	}

	private final MutableLiveData<Event<String>> _signInFlowFeedbackMessage = new MutableLiveData<>();
	public LiveData<Event<String>> getSignInFlowFeedbackMessage() {
		return _signInFlowFeedbackMessage;
	}

	@Inject
	public LoginViewModel(@NonNull Application application,
												AuthenticationRepository authenticationRepository,
												UserRepository userRepository) {
		super(application);
		this.authenticationRepository = authenticationRepository;
		this.userRepository = userRepository;
		this.authState = this.authenticationRepository.getAuthState();
	}

	public void processSignInResult(FirebaseAuthUIAuthenticationResult result) {
		IdpResponse response = result.getIdpResponse();
		if (result.getResultCode() == Activity.RESULT_OK) {
			Log.d(TAG, "Sign-in successful. Waiting for auth state to propagate.");
			// The auth state observer will handle the rest.
		} else {
			if (response == null) {
				_signInFlowFeedbackMessage.setValue(new Event<>("Sign-in cancelled."));
			} else if (response.getError() != null) {
				_operationErrorEvents.setValue(new Event<>("Sign-in error: " + response.getError().getMessage()));
			}
		}
	}

	/**
	 * Called by the fragment's auth state observer when a user is detected.
	 * This method ensures a full profile exists in Firestore.
	 */
	public void onUserAuthenticated(User authUser) {
		if (authUser == null) return;

		userRepository.getOrCreateUserProfile(
				authUser.getId(),
				authUser.getName(),
				authUser.getEmail(),
				null, // photoUrl is not available in your basic User model
				new UserRepository.ProfileOperationCallbacks() {
					@Override
					public void onSuccess(@NonNull User fullUserProfile) {
						_loggedInUserWithProfile.postValue(fullUserProfile);
						_navigationCommand.postValue(new Event<>(new NavigationRoute.LoginToMap()));
					}

					@Override
					public void onError(@NonNull String message) {
						_operationErrorEvents.postValue(new Event<>(message));
					}
				}
		);
	}

	public void onNavigationComplete() {
		_navigationCommand.setValue(null);
	}

	public void startObserving() {
		authenticationRepository.startObservingAuthState();
	}

	public void stopObserving() {
		authenticationRepository.stopObservingAuthState();
	}
}