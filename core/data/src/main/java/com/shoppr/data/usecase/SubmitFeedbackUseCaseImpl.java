package com.shoppr.data.usecase;

import androidx.annotation.NonNull;

import com.shoppr.domain.repository.FeedbackRepository;
import com.shoppr.domain.usecase.SubmitFeedbackUseCase;
import com.shoppr.model.Feedback;

import javax.inject.Inject;

public class SubmitFeedbackUseCaseImpl implements SubmitFeedbackUseCase {

	private final FeedbackRepository feedbackRepository;

	@Inject
	public SubmitFeedbackUseCaseImpl(FeedbackRepository feedbackRepository) {
		this.feedbackRepository = feedbackRepository;
	}

	@Override
	public void execute(@NonNull Feedback feedback, @NonNull SubmitFeedbackCallbacks callbacks) {
		feedbackRepository.submitFeedback(feedback, new FeedbackRepository.SubmitFeedbackCallbacks() {
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
}