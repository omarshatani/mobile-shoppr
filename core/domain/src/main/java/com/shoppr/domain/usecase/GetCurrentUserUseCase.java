package com.shoppr.domain.usecase;

import androidx.lifecycle.LiveData;

import com.shoppr.model.User;

public interface GetCurrentUserUseCase {

	/**
	 * Returns a LiveData object that emits the full User profile from Firestore.
	 * This LiveData will automatically update whenever the user's profile changes.
	 */
    LiveData<User> getFullUserProfile();

    /**
		 * Starts observing the user's profile for real-time updates.
		 */
		void startObserving();

    /**
		 * Stops observing the user's profile to prevent memory leaks.
		 */
		void stopObserving();
}