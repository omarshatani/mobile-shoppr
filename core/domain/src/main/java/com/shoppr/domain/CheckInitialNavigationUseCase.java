package com.shoppr.domain;

import android.util.Log;

import com.shoppr.data.repository.AuthenticationRepository;

import javax.inject.Inject;

public class CheckInitialNavigationUseCase {
    private static final String TAG = "CheckInitialNavigationUseCase";
    public enum InitialTarget {
        MAP_SCREEN,
        LOGIN_SCREEN,
        CHECKOUT_SCREEN
    }


    private final AuthenticationRepository authRepository;

    @Inject
    public CheckInitialNavigationUseCase(AuthenticationRepository authRepository) {
        this.authRepository = authRepository;
    }

    public InitialTarget invoke() {
        // Logic to determine target based on auth state
        if (authRepository.isUserLoggedIn()) {
            return InitialTarget.MAP_SCREEN;
        } else {
            return InitialTarget.LOGIN_SCREEN;
        }
    }
}
