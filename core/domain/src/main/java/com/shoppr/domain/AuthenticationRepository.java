package com.shoppr.domain;

import androidx.lifecycle.LiveData;

import com.shoppr.model.User;

public interface AuthenticationRepository { // Renamed from IAuthenticationRepository
  LiveData<User> getRawAuthState(); // Exposes domain User (basic info from auth) or null

  boolean isUserLoggedIn();

  void logout();

  void startObservingAuthState();

  void stopObservingAuthState();
}