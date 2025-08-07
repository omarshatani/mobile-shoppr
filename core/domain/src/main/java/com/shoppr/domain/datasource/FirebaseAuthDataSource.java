package com.shoppr.domain.datasource;

import androidx.lifecycle.LiveData;

import com.shoppr.model.User;

public interface FirebaseAuthDataSource {
    LiveData<User> getDomainUserAuthStateLiveData();

    void startObserving();

    void stopObserving();

    void signOut();

    boolean isCurrentUserLoggedIn();
}
