package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.model.SuggestedPostDetails;

import java.util.List;

public interface AnalyzePostTextUseCase {

	interface AnalysisCallbacks {
		void onSuccess(@NonNull SuggestedPostDetails suggestions);
		void onError(@NonNull String message);
	}

	/**
	 * Executes the text and image analysis.
	 *
	 * @param rawText The user's natural language input.
	 * @param imageUrls List of URLs for images uploaded by the user (can be empty or null).
	 * @param baseOfferPrice The user's explicitly entered base offer price (optional).
	 * @param baseOfferCurrency The user's explicitly entered currency for the base offer (optional).
	 * @param callbacks Callbacks to return the structured suggestions or an error.
	 */
	void execute(
			@NonNull String rawText,
			@Nullable List<String> imageUrls,
			@Nullable String baseOfferPrice, // Changed to String to match user input field
			@Nullable String baseOfferCurrency,
			@NonNull AnalysisCallbacks callbacks
	);
}