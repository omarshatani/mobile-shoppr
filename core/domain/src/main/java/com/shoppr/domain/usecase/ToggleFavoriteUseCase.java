package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;

public interface ToggleFavoriteUseCase {

	/**
	 * Toggles a post's status in the user's favorites list.
	 *
	 * @param postId    The ID of the post to add or remove from favorites.
	 * @param callbacks The callbacks to be invoked on completion.
	 */
	void execute(
			@NonNull String postId,
			@NonNull FavoriteToggleCallbacks callbacks
	);

	interface FavoriteToggleCallbacks {
		void onSuccess();
		void onError(@NonNull String message);
	}
}