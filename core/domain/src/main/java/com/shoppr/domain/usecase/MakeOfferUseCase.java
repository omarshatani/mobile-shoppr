package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;

import com.shoppr.model.Request;

public interface MakeOfferUseCase {

	interface MakeOfferCallbacks {
		void onSuccess(@NonNull Request createdRequest);

		void onError(@NonNull String message);
	}

	void execute(@NonNull Request request, @NonNull MakeOfferCallbacks callbacks);
}