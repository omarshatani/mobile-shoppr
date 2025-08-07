package com.shoppr;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {

	private final GetCurrentUserUseCase getCurrentUserUseCase;

	// This LiveData now provides the full user profile, not just the auth state.
	public final LiveData<User> authState;

	private final MutableLiveData<Event<NavigationRoute>> _initialNavigationCommand = new MutableLiveData<>();

	public LiveData<Event<NavigationRoute>> getInitialNavigationCommand() {
		return _initialNavigationCommand;
	}

	private final boolean initialCheckCompleted = false;

	@Inject
	public MainViewModel(GetCurrentUserUseCase getCurrentUserUseCase) {
		this.getCurrentUserUseCase = getCurrentUserUseCase;
		this.authState = this.getCurrentUserUseCase.getFullUserProfile();
		startObserving();
	}

	private void startObserving() {
		getCurrentUserUseCase.startObserving();
	}

	private void stopObserving() {
		getCurrentUserUseCase.stopObserving();
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		stopObserving();
	}
}