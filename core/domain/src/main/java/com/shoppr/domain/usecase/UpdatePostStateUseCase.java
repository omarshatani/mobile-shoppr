package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;

import com.shoppr.model.ListingState;

public interface UpdatePostStateUseCase {

	interface UpdatePostStateCallbacks {
		void onSuccess();

		void onError(@NonNull String message);
	}

	void execute(@NonNull String postId, @NonNull ListingState newListingState, @NonNull UpdatePostStateCallbacks callbacks);
}