package com.shoppr.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.model.SuggestedPostDetails;

public interface LLMRepository {
	interface LLMAnalysisCallbacks {
		void onSuccess(SuggestedPostDetails suggestions);

		void onError(String message);
	}

	void getPostSuggestionsFromLLM(
			@NonNull String text,
			@Nullable String baseOfferPrice,
			@Nullable String baseOfferCurrency,
			@NonNull LLMAnalysisCallbacks callbacks
	);
}