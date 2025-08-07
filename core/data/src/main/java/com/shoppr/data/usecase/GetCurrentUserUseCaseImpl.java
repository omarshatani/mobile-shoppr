package com.shoppr.data.usecase; // Or your implementation package

import androidx.lifecycle.LiveData;

import com.shoppr.domain.repository.UserRepository;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.model.User;

import javax.inject.Inject;

public class GetCurrentUserUseCaseImpl implements GetCurrentUserUseCase {

	private final UserRepository userRepository;

    @Inject
		public GetCurrentUserUseCaseImpl(UserRepository userRepository) {
			this.userRepository = userRepository;
    }

    @Override
    public LiveData<User> getFullUserProfile() {
			return userRepository.getFullUserProfile();
    }

    @Override
    public void startObserving() {
			userRepository.startObservingUserProfile();
    }

    @Override
    public void stopObserving() {
			userRepository.stopObservingUserProfile();
    }
}