package com.shoppr.data.usecase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.shoppr.domain.repository.UserRepository;
import com.shoppr.domain.usecase.ToggleFavoriteUseCase;

import javax.inject.Inject;

public class ToggleFavoriteUseCaseImpl implements ToggleFavoriteUseCase {
	private final static String TAG = "ToggleFavoriteUseCase";
	private final UserRepository userRepository;

	@Inject
	public ToggleFavoriteUseCaseImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}


	@Override
	public void execute(@NonNull String postId, boolean isCurrentlyFavorite, @NonNull ToggleFavoriteUseCase.FavoriteToggleCallbacks callbacks) {
		userRepository.toggleFavoriteStatus(postId, isCurrentlyFavorite, new UserRepository.FavoriteToggleCallbacks() {
			@Override
			public void onSuccess(boolean isNowFavorite) {
				callbacks.onSuccess(isNowFavorite);
			}

			@Override
			public void onError(@NonNull String message) {
				Log.e(TAG, "Error toggling favorite: " + message);
				callbacks.onError(message);
			}
		});
	}
}
