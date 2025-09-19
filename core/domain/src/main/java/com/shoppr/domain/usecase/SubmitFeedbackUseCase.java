package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;

import com.shoppr.model.Feedback;

public interface SubmitFeedbackUseCase {

	interface SubmitFeedbackCallbacks {
		void onSuccess();

		void onError(@NonNull String message);
	}

	void execute(@NonNull Feedback feedback, @NonNull SubmitFeedbackCallbacks callbacks);
}