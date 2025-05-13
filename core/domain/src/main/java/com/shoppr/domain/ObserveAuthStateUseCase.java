package com.shoppr.domain;

import androidx.lifecycle.LiveData;

import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;

public interface ObserveAuthStateUseCase {
    LiveData<User> getLoggedInUser();
    void startObserving();
    void stopObserving();
    void setAuthCallbacks(AuthCallbacks callbacks); // For ViewModel to react
    interface AuthCallbacks {
        void onUserAuthenticatedAndProfileReady(User user, NavigationRoute route);
        void onAuthenticationError(String message); // Covers profile errors too
        void onUserLoggedOut(); // If explicit navigation is needed on logout
    }
 }