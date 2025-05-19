package com.shoppr.domain;

import androidx.annotation.NonNull;

import com.shoppr.model.SuggestedPostDetails;

public interface AnalyzePostTextUseCase {

	interface AnalysisCallbacks {
		void onSuccess(@NonNull SuggestedPostDetails suggestions);

		void onError(@NonNull String message);
	}

	/**
	 * Executes the text analysis.
	 *
	 * @param rawText   The user's natural language input.
	 * @param callbacks Callbacks to return the structured suggestions or an error.
	 */
	void execute(@NonNull String rawText, @NonNull AnalysisCallbacks callbacks);
}