package com.shoppr.data.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.domain.repository.UserRepository;
import com.shoppr.domain.usecase.CreateUserProfileUseCase;
import com.shoppr.model.User;

import javax.inject.Inject;

public class CreateUserProfileUseCaseImpl implements CreateUserProfileUseCase {

	private final UserRepository userRepository;

	@Inject
	public CreateUserProfileUseCaseImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public void execute(
			@NonNull String uid,
			@Nullable String displayName,
			@Nullable String email,
			@Nullable String photoUrl,
			@NonNull ProfileOperationCallbacks callbacks
	) {
		// The use case's job is to simply call the repository.
		userRepository.getOrCreateUserProfile(uid, displayName, email, photoUrl, new UserRepository.ProfileOperationCallbacks() {
			@Override
			public void onSuccess(@NonNull User user) {
				callbacks.onSuccess(user);

			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}
}