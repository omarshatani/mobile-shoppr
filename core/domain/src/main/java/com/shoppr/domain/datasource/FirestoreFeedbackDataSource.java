package com.shoppr.domain.datasource;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.shoppr.model.Feedback;

public interface FirestoreFeedbackDataSource {
	interface SubmitFeedbackCallbacks {
		void onSuccess();

		void onError(@NonNull String message);
	}

	void submitFeedback(@NonNull Feedback feedback, @NonNull SubmitFeedbackCallbacks callbacks);

	LiveData<Boolean> hasUserGivenFeedback(@NonNull String requestId, @NonNull String raterId);
}