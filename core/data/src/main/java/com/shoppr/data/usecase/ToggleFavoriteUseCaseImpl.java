package com.shoppr.data.usecase;

import androidx.annotation.NonNull;

import com.shoppr.domain.repository.UserRepository;
import com.shoppr.domain.usecase.ToggleFavoriteUseCase;

import javax.inject.Inject;

public class ToggleFavoriteUseCaseImpl implements ToggleFavoriteUseCase {

	private final UserRepository userRepository;

	@Inject
	public ToggleFavoriteUseCaseImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public void execute(
			@NonNull String postId,
			@NonNull FavoriteToggleCallbacks callbacks
	) {
		userRepository.toggleFavoriteStatus(postId, new UserRepository.OperationCallbacks() {
			@Override
			public void onSuccess() {
				callbacks.onSuccess();
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}
}