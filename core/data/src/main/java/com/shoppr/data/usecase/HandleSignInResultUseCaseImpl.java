package com.shoppr.data.usecase;

import android.util.Log;

import androidx.annotation.Nullable;

import com.shoppr.domain.HandleSignInResultUseCase;

import javax.inject.Inject;

public class HandleSignInResultUseCaseImpl implements HandleSignInResultUseCase {
	@Inject
	public HandleSignInResultUseCaseImpl() {
	}

	@Override
	public void process(boolean isSuccess, boolean isCancellation, @Nullable String errorMessage,
						HandleSignInResultUseCase.SignInResultCallbacks callbacks) {
		Log.d("HandleSignInResultUCImpl", "Processing. Success: " + isSuccess + ", Cancelled: " + isCancellation);
		if (isSuccess) {
			callbacks.onSuccess();
		} else if (isCancellation) {
			callbacks.onCancelled();
		} else {
			callbacks.onError(errorMessage != null ? errorMessage : "Unknown sign-in error");
		}
	}
}