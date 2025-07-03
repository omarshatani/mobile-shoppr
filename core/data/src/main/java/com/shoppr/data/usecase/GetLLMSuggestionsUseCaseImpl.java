package com.shoppr.data.usecase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.domain.repository.LLMRepository;
import com.shoppr.domain.usecase.GetLLMSuggestionsUseCase;
import com.shoppr.model.SuggestedPostDetails;

import java.util.List;

import javax.inject.Inject;

public class GetLLMSuggestionsUseCaseImpl implements GetLLMSuggestionsUseCase {
	private static final String TAG = "GetLLMSuggestionsUseCaseImpl";
	private final LLMRepository llmRepository; // Depends on the repository interface

	@Inject
	public GetLLMSuggestionsUseCaseImpl(LLMRepository llmRepository) {
		this.llmRepository = llmRepository;
	}

	@Override
	public void execute(
			@NonNull String rawText,
			@Nullable List<String> imageUrls,
			@Nullable String baseOfferPrice,
			@Nullable String baseOfferCurrency,
			@NonNull final AnalysisCallbacks useCaseCallbacks) {

		Log.d(TAG, "Executing text analysis. Text: \"" + rawText +
				"\", Image URLs count: " + (imageUrls != null ? imageUrls.size() : 0) +
				", Price: " + baseOfferPrice +
				", Currency: " + baseOfferCurrency);

		if (rawText.trim().isEmpty()) {
			useCaseCallbacks.onError("Input text cannot be empty.");
			return;
		}

		// Delegate to the repository to handle the actual Cloud Function call
		llmRepository.getPostSuggestionsFromLLM(rawText, imageUrls, baseOfferPrice, baseOfferCurrency, new LLMRepository.LLMAnalysisCallbacks() {
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