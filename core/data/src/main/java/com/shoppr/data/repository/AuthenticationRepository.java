package com.shoppr.data.repository;

import androidx.lifecycle.LiveData;

import com.shoppr.model.User;

 public interface AuthenticationRepository {
    LiveData<User> getAuthState();
    boolean isUserLoggedIn();
    void logout();
    void startObservingAuthState();
    void stopObservingAuthState();
 }