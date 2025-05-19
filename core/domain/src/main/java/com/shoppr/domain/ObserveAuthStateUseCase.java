package com.shoppr.domain;

import androidx.lifecycle.LiveData;

import com.shoppr.model.User;

public interface ObserveAuthStateUseCase {
	LiveData<User> getRawAuthUser(); // Exposes basic User from AuthenticationRepository
	// No longer exposes getLoggedInUserWithProfile directly or authenticationErrorEvents
	// These are now handled by LoginViewModel using CreateUserProfileUseCase
	void startObserving();
	void stopObserving();
}