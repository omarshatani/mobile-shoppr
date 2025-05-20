package com.shoppr.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.model.SuggestedPostDetails;

public interface AnalyzePostTextUseCase {

	interface AnalysisCallbacks {
		void onSuccess(@NonNull SuggestedPostDetails suggestions);
		void onError(@NonNull String message);
	}

	void execute(
			@NonNull String rawText,
			@Nullable Double baseOfferPrice,
			@Nullable String baseOfferCurrency,
			@NonNull AnalysisCallbacks callbacks
	);
}