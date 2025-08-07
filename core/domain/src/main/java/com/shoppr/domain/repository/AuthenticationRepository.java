package com.shoppr.domain.repository;

import androidx.lifecycle.LiveData;
import com.shoppr.model.User;

public interface AuthenticationRepository {

  /**
   * Returns a LiveData object that emits a basic User object from the auth provider.
   * This User object will contain basic info like UID and email, but NOT the full profile.
   * It will emit null if the user is logged out.
   */
  LiveData<User> getAuthState();

  /**
   * A synchronous method to check if a user is currently logged in.
   */
  boolean isUserLoggedIn();

  /**
   * Signs the current user out.
   */
  void logout();

  /**
   * Starts observing changes in the user's authentication state.
   */
  void startObservingAuthState();

  /**
   * Stops observing changes in the user's authentication state.
   */
  void stopObservingAuthState();
}