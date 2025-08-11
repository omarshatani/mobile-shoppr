package com.shoppr.ui;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.repository.AuthenticationRepository;
import com.shoppr.model.Event;
import com.shoppr.navigation.NavigationRoute;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SplashViewModel extends ViewModel {
	private static final String TAG = "SplashViewModel";
	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();
	public LiveData<Event<NavigationRoute>> navigation = _navigationCommand;

	@Inject

	public SplashViewModel(AuthenticationRepository authenticationRepository) {
		if (authenticationRepository.isUserLoggedIn()) {
			Log.d(TAG, String.valueOf(authenticationRepository.isUserLoggedIn()));
			_navigationCommand.postValue(new Event<>(new NavigationRoute.SplashToMain()));
		} else {
			_navigationCommand.postValue(new Event<>(new NavigationRoute.SplashToLogin()));
		}
	}
}