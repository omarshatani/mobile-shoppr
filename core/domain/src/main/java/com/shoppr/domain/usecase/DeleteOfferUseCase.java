package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;

import com.shoppr.model.Request;

public interface DeleteOfferUseCase {

	interface DeleteOfferCallbacks {
		void onSuccess();

		void onError(@NonNull String message);
	}

	void execute(@NonNull Request request, @NonNull DeleteOfferCallbacks callbacks);
}