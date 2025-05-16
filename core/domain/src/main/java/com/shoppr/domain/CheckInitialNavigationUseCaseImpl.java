package com.shoppr.domain;

import com.shoppr.data.repository.AuthenticationRepository;
import com.shoppr.navigation.InitialTarget;

import javax.inject.Inject;

public class CheckInitialNavigationUseCaseImpl implements CheckInitialNavigationUseCase {
    private static final String TAG = "CheckInitialNavigationUseCase";
    private final AuthenticationRepository authRepository;

    @Inject
    public CheckInitialNavigationUseCaseImpl(AuthenticationRepository authRepository) {
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
