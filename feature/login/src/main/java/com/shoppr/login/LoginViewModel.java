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
import com.shoppr.model.Event;
import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends AndroidViewModel {
	private static final String TAG = "LoginViewModel";

	private final ObserveAuthStateUseCase observeAuthStateUseCase;
	private final HandleSignInResultUseCase handleSignInResultUseCase;

	public final LiveData<User> loggedInUserWithProfileLiveData;
	public final LiveData<Event<String>> authenticationErrorEvents;

	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();

	public LiveData<Event<NavigationRoute>> getNavigationCommand() {
		return _navigationCommand;
	}

	private final MutableLiveData<Event<String>> _signInFlowToastMessage = new MutableLiveData<>();

	public LiveData<Event<String>> getSignInFlowToastMessage() {
		return _signInFlowToastMessage;
	}

	// Removed _isSignInFlowCurrentlyActive LiveData. Fragment manages its hasLaunchedSignIn flag.

	// Flag to indicate a login attempt (FirebaseUI returned RESULT_OK) has been made
	// and we are now waiting for the ObserveAuthStateUseCase to provide the full User profile.
	private boolean waitingForUserProfile = false;


	@Inject
	public LoginViewModel(@NonNull Application application,
												ObserveAuthStateUseCase observeAuthStateUseCase,
												HandleSignInResultUseCase handleSignInResultUseCase) {
		super(application);
		this.observeAuthStateUseCase = observeAuthStateUseCase;
		this.handleSignInResultUseCase = handleSignInResultUseCase;

		this.loggedInUserWithProfileLiveData = this.observeAuthStateUseCase.getLoggedInUserWithProfile();
		this.authenticationErrorEvents = this.observeAuthStateUseCase.getAuthenticationErrorEvents();

	}

	private void observeAuthState() {
		this.observeAuthStateUseCase.getLoggedInUserWithProfile().observeForever(this::verifyUserStateAndNavigate);
	}

	private void verifyUserStateAndNavigate(User user) {
		if (waitingForUserProfile) {
			return;
		}

		if (user != null) {
			Log.d(TAG, "LoginViewModel: loggedInUserWithProfileLiveData changed. User: " + user.getId() + ", waitingForProfile: " + waitingForUserProfile);
			_navigationCommand.postValue(new Event<>(new NavigationRoute.LoginToMap()));
			waitingForUserProfile = false;
			observeAuthStateUseCase.getLoggedInUserWithProfile().removeObserver(this::verifyUserStateAndNavigate);
		}
	}

	public void registerAuthStateListener() {
		Log.d(TAG, "LoginViewModel: Telling ObserveAuthStateUseCase to start observing.");
		observeAuthStateUseCase.startObserving();
	}

	public void unregisterAuthStateListener() {
		Log.d(TAG, "LoginViewModel: Telling ObserveAuthStateUseCase to stop observing.");
		observeAuthStateUseCase.getLoggedInUserWithProfile().removeObserver(this::verifyUserStateAndNavigate);
		observeAuthStateUseCase.stopObserving();
	}

	public void processSignInResult(FirebaseAuthUIAuthenticationResult firebaseResult) {
		Log.d(TAG, "LoginViewModel: processSignInResult from Fragment. ResultCode: " + firebaseResult.getResultCode());
		waitingForUserProfile = true;

		observeAuthState();
	}

	public void onNavigationComplete() {
		Log.d(TAG, "onNavigationComplete: Resetting navigation route.");
		_navigationCommand.postValue(null);
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		Log.d(TAG, "onCleared: LoginViewModel is being cleared.");
		// If using observeForever for loggedInUserWithProfileLiveData, remove observer here
		observeAuthStateUseCase.getLoggedInUserWithProfile().removeObserver(this::verifyUserStateAndNavigate);
	}
}