package com.shoppr.domain.usecase;

import androidx.lifecycle.LiveData;

import com.shoppr.model.Event;
import com.shoppr.model.User;

public interface GetCurrentUserUseCase {
    /**
     * Gets a LiveData stream that emits the full User domain object (including profile details
     * from Firestore and last known location) when a user is authenticated and their
     * profile is ready. Emits null if no user is authenticated or if profile
     * retrieval/creation fails.
     */
    LiveData<User> getFullUserProfile();

    /**
     * Exposes errors that might occur during the profile fetching/creation part
     * that is orchestrated by this use case.
     */
    LiveData<Event<String>> getProfileErrorEvents();

    /**
     * Call this when the ViewModel observing this use case is ready to receive updates.
     * This will trigger the underlying auth observation and profile fetching if it's not already active.
     */
    void startObserving();

    /**
     * Call this when the ViewModel is no longer interested in updates to conserve resources.
     */
    void stopObserving();
}