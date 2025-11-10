package com.shoppr.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.shoppr.domain.datasource.FirestoreFeedbackDataSource;
import com.shoppr.domain.repository.FeedbackRepository;
import com.shoppr.model.Feedback;

import javax.inject.Inject;

public class FeedbackRepositoryImpl implements FeedbackRepository {

	private final FirestoreFeedbackDataSource firestoreFeedbackDataSource;

	@Inject
	public FeedbackRepositoryImpl(FirestoreFeedbackDataSource firestoreFeedbackDataSource) {
		this.firestoreFeedbackDataSource = firestoreFeedbackDataSource;
	}

	@Override
	public void submitFeedback(@NonNull Feedback feedback, @NonNull SubmitFeedbackCallbacks callbacks) {
		firestoreFeedbackDataSource.submitFeedback(feedback, new FirestoreFeedbackDataSource.SubmitFeedbackCallbacks() {
			@Override
			public void onSuccess() {
				callbacks.onSuccess();
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}

	@Override
	public LiveData<Boolean> hasUserGivenFeedback(@NonNull String requestId, @NonNull String raterId) {
		return firestoreFeedbackDataSource.hasUserGivenFeedback(requestId, raterId);
	}
}