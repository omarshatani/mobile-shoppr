package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.model.Request;

public interface GetRequestForPostUseCase {

	interface GetRequestForPostCallbacks {
		void onSuccess(@Nullable Request request);

		void onError(@NonNull String message);
	}

	void execute(String userId, String postId, GetRequestForPostCallbacks callbacks);
}