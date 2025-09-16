package com.shoppr.data.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.domain.repository.UserRepository;
import com.shoppr.domain.usecase.GetUserByIdUseCase;
import com.shoppr.model.User;

import javax.inject.Inject;

public class GetUserByIdUseCaseImpl implements GetUserByIdUseCase {

	private final UserRepository userRepository;

	@Inject
	public GetUserByIdUseCaseImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public void execute(String userId, @NonNull GetUserByIdCallbacks callbacks) {
		userRepository.getUserById(userId, new UserRepository.GetUserByIdCallbacks() {
			@Override
			public void onSuccess(@Nullable User user) {
				callbacks.onSuccess(user);
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}
}