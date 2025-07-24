package com.shoppr.domain.usecase;

import androidx.lifecycle.LiveData;

import com.shoppr.model.Event;
import com.shoppr.model.User;

public interface GetCurrentUserUseCase {
    LiveData<User> getFullUserProfile();
    LiveData<Event<String>> getProfileErrorEvents();
    void startObserving();
    void stopObserving();
}