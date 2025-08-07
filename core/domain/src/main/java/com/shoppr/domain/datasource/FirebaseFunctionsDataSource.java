package com.shoppr.domain.datasource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.model.SuggestedPostDetails;

public interface FirebaseFunctionsDataSource {

	interface LLMCallbacks {
		void onSuccess(@NonNull SuggestedPostDetails suggestions);

		void onError(@NonNull String message);
	}

	void getPostSuggestions(
			@NonNull String text,
			@Nullable String baseOfferPrice,
			@Nullable String baseOfferCurrency,
			@NonNull LLMCallbacks callbacks
	);
}