package com.shoppr;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

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
	public final LiveData<User> rawAuthUserLiveData; // MainVM observes raw auth state
	// Potentially, MainVM could also use CreateUserProfileUseCase if it needs the full profile for its UI

	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();

	public LiveData<Event<NavigationRoute>> getNavigationCommand() {
		return _navigationCommand;
	}

	private boolean initialCheckDone = false;
	private final Observer<User> mainAuthStateObserver;

	@Inject
	public MainViewModel(@NonNull Application application, ObserveAuthStateUseCase observeAuthStateUseCase) {
		super(application);
		this.observeAuthStateUseCase = observeAuthStateUseCase;
		this.rawAuthUserLiveData = this.observeAuthStateUseCase.getRawAuthUser();

		mainAuthStateObserver = rawUser -> {
			Log.d(TAG, "MainViewModel: Observed rawAuthUser. User: " + (rawUser != null ? rawUser.getId() : "null"));
			if (!initialCheckDone) {
				initialCheckDone = true;
				if (rawUser != null) {
					Log.d(TAG, "Initial check: Raw user logged in. Navigating to SplashToMap.");
					_navigationCommand.postValue(new Event<>(new NavigationRoute.SplashToMap()));
				} else {
					Log.d(TAG, "Initial check: Raw user not logged in. Navigating to SplashToLogin.");
					_navigationCommand.postValue(new Event<>(new NavigationRoute.SplashToLogin()));
				}
			} else if (rawUser == null) {
				Log.d(TAG, "Raw user logged out (detected by MainViewModel). Triggering navigation to Login.");
				_navigationCommand.postValue(new Event<>(new NavigationRoute.Login()));
			}
		};
	}

	public void startGlobalAuthObservation() {
		Log.d(TAG, "MainViewModel: Telling ObserveAuthStateUseCase to start observing AND adding local observer to rawAuthUserLiveData.");
		observeAuthStateUseCase.startObserving();
		this.rawAuthUserLiveData.observeForever(mainAuthStateObserver);
	}

	public void stopGlobalAuthObservation() {
		Log.d(TAG, "MainViewModel: Telling ObserveAuthStateUseCase to stop observing AND removing local observer from rawAuthUserLiveData.");
		this.rawAuthUserLiveData.removeObserver(mainAuthStateObserver);
		observeAuthStateUseCase.stopObserving();
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		Log.d(TAG, "onCleared: MainViewModel is being cleared. Removing observer.");
		this.rawAuthUserLiveData.removeObserver(mainAuthStateObserver);
	}
}