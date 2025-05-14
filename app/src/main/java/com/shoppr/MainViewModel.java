package com.shoppr;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shoppr.domain.ObserveAuthStateUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends AndroidViewModel {
	private static final String TAG = "MainViewModel";
	private final ObserveAuthStateUseCase observeAuthStateUseCase;
	public final LiveData<Event<String>> authenticationErrorEvents;

	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();

	public LiveData<Event<NavigationRoute>> getNavigationCommand() {
		return _navigationCommand;
	}

	private boolean initialCheckDone = false;
	private final androidx.lifecycle.Observer<User> authStateObserver; // To manage observeForever

	@Inject
	public MainViewModel(@NonNull Application application, ObserveAuthStateUseCase observeAuthStateUseCase) {
		super(application);
		this.observeAuthStateUseCase = observeAuthStateUseCase;
		this.authenticationErrorEvents = this.observeAuthStateUseCase.getAuthenticationErrorEvents();

		authStateObserver = user -> {
			Log.d(TAG, "MainViewModel: Observed loggedInUserWithProfile. User: " + (user != null ? user.getId() : "null"));
			if (!initialCheckDone) {
				initialCheckDone = true;
				if (user != null) {
					Log.d(TAG, "Initial check: User logged in. Navigating to SplashToMap.");
					_navigationCommand.postValue(new Event<>(new NavigationRoute.SplashToMap()));
				} else {
					Log.d(TAG, "Initial check: User not logged in. Navigating to SplashToLogin.");
					_navigationCommand.postValue(new Event<>(new NavigationRoute.SplashToLogin()));
				}
			} else if (user == null) {
				Log.d(TAG, "User logged out (detected by MainViewModel). Triggering navigation to Login.");
				_navigationCommand.postValue(new Event<>(new NavigationRoute.Login()));
			}
		};

		observeAuthStateUseCase.getLoggedInUserWithProfile().observeForever(authStateObserver);
	}

	public void startAuthObservation() {
		Log.d(TAG, "MainViewModel: Telling ObserveAuthStateUseCase to start observing.");
		observeAuthStateUseCase.startObserving();
	}

	public void stopAuthObservation() {
		Log.d(TAG, "MainViewModel: Telling ObserveAuthStateUseCase to stop observing.");
		observeAuthStateUseCase.stopObserving();
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		Log.d(TAG, "onCleared: MainViewModel is being cleared.");
		observeAuthStateUseCase.getLoggedInUserWithProfile().removeObserver(authStateObserver); // Clean up observeForever
	}
}