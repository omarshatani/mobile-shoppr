package com.shoppr.domain;

import androidx.lifecycle.LiveData;

import com.shoppr.model.Event;
import com.shoppr.model.User;

public interface ObserveAuthStateUseCase {
	LiveData<User> getLoggedInUserWithProfile(); // Emits full User (after profile) or null

	LiveData<Event<String>> getAuthenticationErrorEvents(); // For errors during auth/profile process

	void startObserving();

	void stopObserving();
}