package com.shoppr.domain;

import androidx.lifecycle.LiveData;

import com.shoppr.model.Event;
import com.shoppr.model.User;

public interface GetCurrentUserUseCase {
    /**
     * Executes the logic to get the current authenticated user with their full profile.
     * This LiveData will emit the full User object from Firestore after authentication
     * and profile retrieval/creation, or null if not authenticated or profile error.
     */
    LiveData<User> getFullUserProfile(); // Renamed for clarity

    /**
     * Exposes errors that might occur during the profile fetching/creation part
     * that is orchestrated by this use case.
     */
    LiveData<Event<String>> getProfileErrorEvents();

    /**
     * Call this when the ViewModel observing this use case is ready to receive updates.
     * This will trigger the underlying auth observation if it's not already active.
     */
    void startObserving();

    /**
     * Call this when the ViewModel is no longer interested in updates.
     */
    void stopObserving();
}