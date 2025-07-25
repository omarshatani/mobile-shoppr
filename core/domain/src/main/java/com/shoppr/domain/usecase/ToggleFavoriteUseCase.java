package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;

public interface ToggleFavoriteUseCase {

	void execute(
			@NonNull String postId,
			boolean isCurrentlyFavorite,
			@NonNull FavoriteToggleCallbacks callbacks
	);

	interface FavoriteToggleCallbacks {
		void onSuccess(boolean isNowFavorite);

		void onError(@NonNull String message);
	}
}