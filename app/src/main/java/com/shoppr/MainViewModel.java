package com.shoppr;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shoppr.domain.ObserveAuthStateUseCase;
import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;
import com.shoppr.ui.utils.Event;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends AndroidViewModel implements ObserveAuthStateUseCase.AuthCallbacks {
	private static final String TAG = "MainViewModel";
	private final ObserveAuthStateUseCase observeAuthStateUseCase;
	public final LiveData<User> loggedInUserLiveData;

	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();

	public LiveData<Event<NavigationRoute>> getNavigationCommand() {
		return _navigationCommand;
	}

	@Inject
	public MainViewModel(@NonNull Application application, ObserveAuthStateUseCase observeAuthStateUseCase) {
		super(application);
		this.observeAuthStateUseCase = observeAuthStateUseCase;
		this.observeAuthStateUseCase.setAuthCallbacks(this); // MainViewModel handles global auth reactions
		this.loggedInUserLiveData = this.observeAuthStateUseCase.getLoggedInUser();
	}

	public void startAuthObservation() {
		observeAuthStateUseCase.startObserving();
	}

	public void stopAuthObservation() {
		observeAuthStateUseCase.stopObserving();
	}

	@Override
	public void onUserAuthenticatedAndProfileReady(User user) { // Route parameter removed
		Log.d(TAG, "MainViewModel: onUserAuthenticatedAndProfileReady. User: " + user.getId());
		// This callback might be hit if MainViewModel is the *only* callback listener,
		// or if ObserveAuthStateUseCase calls back all listeners.
		// If LoginViewModel handles LoginToMap, MainViewModel might not need to do anything here,
		// or it could verify app is in a correct state.
	}

	@Override
	public void onAuthenticationError(String message) {
		Log.e(TAG, "MainViewModel: onAuthenticationError. Message: " + message);
		// Optionally show a global toast or error state via its own LiveData
	}

	@Override
	public void onUserLoggedOut() {
		Log.d(TAG, "MainViewModel: onUserLoggedOut. User is now null. Triggering navigation to Login.");
		_navigationCommand.setValue(new Event<>(new NavigationRoute.Login())); // Navigate to Login
	}

	public void onNavigationEventHandled() { /* Called by MainActivity */ }
}