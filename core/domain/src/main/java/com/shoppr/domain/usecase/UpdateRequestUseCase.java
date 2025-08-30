package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;

import com.shoppr.model.Request;

public interface UpdateRequestUseCase {

	interface UpdateRequestCallbacks {
		void onSuccess();

		void onError(@NonNull String message);
	}

	void execute(@NonNull Request request, @NonNull UpdateRequestCallbacks callbacks);
}