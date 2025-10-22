package com.shoppr.data.usecase;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.shoppr.domain.repository.FeedbackRepository;
import com.shoppr.domain.usecase.HasUserGivenFeedbackUseCase;

import javax.inject.Inject;

public class HasUserGivenFeedbackUseCaseImpl implements HasUserGivenFeedbackUseCase {

	private final FeedbackRepository feedbackRepository;

	@Inject
	public HasUserGivenFeedbackUseCaseImpl(FeedbackRepository feedbackRepository) {
		this.feedbackRepository = feedbackRepository;
	}

	@Override
	public LiveData<Boolean> execute(@NonNull String requestId, @NonNull String raterId) {
		return feedbackRepository.hasUserGivenFeedback(requestId, raterId);
	}
}