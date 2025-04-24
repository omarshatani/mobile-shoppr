package com.shoppr.domain;

import com.shoppr.data.repository.AuthenticationRepository;

import javax.inject.Inject;

public class CheckInitialNavigationUseCase {
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
        if (authRepository.isUserLoggedIn()) { // Assuming repo has this method
            return InitialTarget.MAP_SCREEN;
        } else {
            return InitialTarget.LOGIN_SCREEN;
        }
    }
}
