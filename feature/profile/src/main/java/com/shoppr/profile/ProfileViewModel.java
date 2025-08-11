package com.shoppr.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.LogoutUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {
	private static final String TAG = "ProfileViewModel";

	private final GetCurrentUserUseCase getCurrentUserUseCase;
	private final LogoutUseCase logoutUseCase;

	public final LiveData<User> currentUserProfile;

	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();
	public LiveData<Event<NavigationRoute>> getNavigationCommand() {
		return _navigationCommand;
	}

	@Inject
	public ProfileViewModel(
			GetCurrentUserUseCase getCurrentUserUseCase,
			LogoutUseCase logoutUseCase
	) {
		this.getCurrentUserUseCase = getCurrentUserUseCase;
		this.logoutUseCase = logoutUseCase;
		this.currentUserProfile = this.getCurrentUserUseCase.getFullUserProfile();
	}

	public void onFragmentStarted() {
		getCurrentUserUseCase.startObserving();
	}

	public void onFragmentStopped() {
		getCurrentUserUseCase.stopObserving();
	}

	public void onLogoutClicked() {
		Log.d(TAG, "Logout button clicked. Invoking LogoutUseCase.");
		logoutUseCase.invoke();
		_navigationCommand.setValue(new Event<>(new NavigationRoute.ProfileToLogin()));
	}

	public void onMyFavoritesClicked() {
		Log.d(TAG, "My Favorites clicked. Navigating to ProfileToFavorites.");
		_navigationCommand.setValue(new Event<>(new NavigationRoute.ProfileToFavorites()));
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		getCurrentUserUseCase.stopObserving();
	}
}