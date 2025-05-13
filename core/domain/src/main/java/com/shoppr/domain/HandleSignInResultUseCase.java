package com.shoppr.domain;

import androidx.annotation.Nullable;

public interface HandleSignInResultUseCase {
	interface SignInResultCallbacks {
		void onSuccess(); // UI flow success

		void onCancelled();

		void onError(String message);
	}

	void process(boolean isSuccess, boolean isCancellation, @Nullable String errorMessage, SignInResultCallbacks callbacks);
}