package com.shoppr.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.LogoutUseCase;
import com.shoppr.model.Event;
import com.shoppr.navigation.NavigationRoute;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {
	private static final String TAG = "ProfileViewModel";
	private final LogoutUseCase logoutUseCase;

	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();

	public LiveData<Event<NavigationRoute>> getNavigationCommand() {
		return _navigationCommand;
	}

	@Inject
	public ProfileViewModel(LogoutUseCase logoutUseCase) {
		this.logoutUseCase = logoutUseCase;
	}

	public void onLogoutClicked() {
		Log.d(TAG, "Logout button clicked. Invoking LogoutUseCase and then navigating to ProfileToLogin.");
		logoutUseCase.invoke();
		_navigationCommand.setValue(new Event<>(new NavigationRoute.ProfileToLogin()));
	}
}