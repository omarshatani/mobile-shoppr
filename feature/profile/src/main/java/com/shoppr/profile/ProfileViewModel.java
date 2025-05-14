package com.shoppr.profile;

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

	@Inject
	public ProfileViewModel(LogoutUseCase logoutUseCase) {
		this.logoutUseCase = logoutUseCase;
	}

	public void logout() {
		logoutUseCase.invoke();
	}
}