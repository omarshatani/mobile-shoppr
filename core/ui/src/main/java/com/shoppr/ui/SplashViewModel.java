package com.shoppr.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.CheckInitialNavigationUseCase;
import com.shoppr.model.Event;
import com.shoppr.navigation.InitialTarget;
import com.shoppr.navigation.NavigationRoute;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SplashViewModel extends ViewModel {
	private final CheckInitialNavigationUseCase checkInitialNavigationUseCase;

	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();
	public LiveData<Event<NavigationRoute>> navigation = _navigationCommand;

	@Inject
	public SplashViewModel(CheckInitialNavigationUseCase checkInitialNavigationUseCase) {
		this.checkInitialNavigationUseCase = checkInitialNavigationUseCase;
		// Trigger the check immediately when ViewModel is created
		checkInitialState();
	}

	private void checkInitialState() {
		InitialTarget target = checkInitialNavigationUseCase.invoke();
		NavigationRoute targetRoute = null;
		switch (target) {
			case MAP_SCREEN:
				targetRoute = new NavigationRoute.SplashToMap();
				break;
			case LOGIN_SCREEN:
				targetRoute = new NavigationRoute.SplashToLogin();
				break;
		}
		if (targetRoute != null) {
			// Post the navigation command as a single event
			_navigationCommand.postValue(new Event<>(targetRoute));
		} else {
			// Handle error case - maybe navigate to Login by default?
			_navigationCommand.postValue(new Event<>(new NavigationRoute.SplashToLogin()));
		}
	}
}