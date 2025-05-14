package com.shoppr.domain;

import androidx.lifecycle.LiveData;

import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;

public interface ObserveAuthStateUseCase {
	LiveData<User> getLoggedInUser();

	void startObserving();

	void stopObserving();

	interface AuthCallbacks {
		void onUserAuthenticatedAndProfileReady(User user); // Route decision now made by ViewModel

		void onAuthenticationError(String message);

		void onUserLoggedOut();
	}

	void setAuthCallbacks(AuthCallbacks callbacks);
}