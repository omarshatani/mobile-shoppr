package com.shoppr.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.LogoutUseCase;
import com.shoppr.navigation.NavigationRoute;
import com.shoppr.ui.utils.Event;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {
	private final LogoutUseCase logoutUseCase;
	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();
	public LiveData<Event<NavigationRoute>> navigationCommand = _navigationCommand;

	@Inject
	public ProfileViewModel(LogoutUseCase logoutUseCase) {
		this.logoutUseCase = logoutUseCase;
	}

	public void logout() {
		logoutUseCase.invoke();
		_navigationCommand.setValue(new Event<>(new NavigationRoute.ProfileToLogin()));
	}
}