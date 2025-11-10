package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.model.User;

public interface GetUserByIdUseCase {

	interface GetUserByIdCallbacks {
		void onSuccess(@Nullable User user);

		void onError(@NonNull String message);
	}

	void execute(String userId, @NonNull GetUserByIdCallbacks callbacks);
}