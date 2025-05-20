package com.shoppr.domain;

import androidx.annotation.Nullable;

import com.shoppr.model.User;

public interface GetCurrentUserUseCase {
    /**
     * Executes the logic to get the current authenticated user with their full profile.
     *
     * @return The current User domain object if authenticated and profile is available,
     * otherwise null. This could also return LiveData<User> if the underlying
     * source (like ObserveAuthStateUseCase) is reactive.
     * For a simpler synchronous-like contract from ViewModel, returning User directly
     * means the use case implementation might need to fetch it synchronously if not cached,
     * or the ViewModel needs to observe the LiveData from ObserveAuthStateUseCase.
     * <p>
     * Let's make this return LiveData<User> to align with ObserveAuthStateUseCase.
     */
    // LiveData<User> execute();
    // Alternative synchronous-like signature (implementation would need to handle state)
    @Nullable
    User execute();
}