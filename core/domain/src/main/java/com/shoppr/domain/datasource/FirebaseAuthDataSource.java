package com.shoppr.domain.datasource;

import androidx.lifecycle.LiveData;

import com.shoppr.model.User;

public interface FirebaseAuthDataSource { // This is the interface for the domain to depend on
    LiveData<User> getDomainUserAuthStateLiveData(); // Exposes domain User

    void startObserving();

    void stopObserving();

    void signOut();

    boolean isCurrentUserLoggedIn(); // For synchronous check
}
