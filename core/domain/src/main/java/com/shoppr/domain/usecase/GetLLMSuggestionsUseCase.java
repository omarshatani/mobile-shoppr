package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.model.SuggestedPostDetails;

import java.util.List;

public interface GetLLMSuggestionsUseCase {

	interface LLMAnalysisCallbacks {
		void onSuccess(SuggestedPostDetails suggestions);

		void onError(String message);
	}

	void execute(
			@NonNull String text,
			@Nullable List<String> imageUrls,
			@Nullable String baseOfferPrice,
			@Nullable String baseOfferCurrency,
			@NonNull LLMAnalysisCallbacks callbacks
	);
}