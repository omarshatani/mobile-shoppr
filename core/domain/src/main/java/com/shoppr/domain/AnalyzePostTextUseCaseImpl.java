package com.shoppr.domain;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.data.repository.LLMRepository;
import com.shoppr.model.SuggestedPostDetails;

import javax.inject.Inject;

public class AnalyzePostTextUseCaseImpl implements AnalyzePostTextUseCase {
	private static final String TAG = "AnalyzePostTextUCImpl";
	private final LLMRepository llmRepository; // Depends on the repository interface

	@Inject
	public AnalyzePostTextUseCaseImpl(LLMRepository llmRepository) {
		this.llmRepository = llmRepository;
	}

	@Override
	public void execute(
			@NonNull String rawText,
			@Nullable Double baseOfferPrice,
			@Nullable String baseOfferCurrency,
			@NonNull final AnalysisCallbacks useCaseCallbacks) {

		Log.d(TAG, "Executing text analysis for: \"" + rawText + "\", Price: " + baseOfferPrice + ", Currency: " + baseOfferCurrency);

		if (rawText.trim().isEmpty()) {
			useCaseCallbacks.onError("Input text cannot be empty.");
			return;
		}

		// Delegate to the repository to handle the actual Cloud Function call
		llmRepository.analyzeTextForPost(rawText, baseOfferPrice, baseOfferCurrency, new LLMRepository.LLMAnalysisCallbacks() {
			@Override
			public void onSuccess(@NonNull SuggestedPostDetails suggestions) {
				Log.d(TAG, "LLMRepository returned success. Title: " + suggestions.getSuggestedTitle());
				useCaseCallbacks.onSuccess(suggestions);
			}

			@Override
			public void onError(@NonNull String message) {
				Log.e(TAG, "LLMRepository returned error: " + message);
				useCaseCallbacks.onError(message);
			}
		});
	}
}