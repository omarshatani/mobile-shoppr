package com.shoppr.domain.repository;

import androidx.annotation.NonNull;

import com.shoppr.model.Feedback;

public interface FeedbackRepository {
	interface SubmitFeedbackCallbacks {
		void onSuccess();

		void onError(@NonNull String message);
	}

	void submitFeedback(@NonNull Feedback feedback, @NonNull SubmitFeedbackCallbacks callbacks);
}