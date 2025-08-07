package com.shoppr.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.domain.datasource.FirebaseFunctionsDataSource;
import com.shoppr.domain.repository.LLMRepository;
import com.shoppr.model.SuggestedPostDetails;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LLMRepositoryImpl implements LLMRepository {

	private final FirebaseFunctionsDataSource functionsDataSource;

	@Inject
	public LLMRepositoryImpl(FirebaseFunctionsDataSource functionsDataSource) {
		this.functionsDataSource = functionsDataSource;
	}

	@Override
	public void getPostSuggestionsFromLLM(
			@NonNull String text,
			@Nullable String baseOfferPrice,
			@Nullable String baseOfferCurrency,
			@NonNull LLMAnalysisCallbacks callbacks
	) {
		functionsDataSource.getPostSuggestions(text, baseOfferPrice, baseOfferCurrency, new FirebaseFunctionsDataSource.LLMCallbacks() {
			@Override
			public void onSuccess(@NonNull SuggestedPostDetails suggestions) {
				callbacks.onSuccess(suggestions);
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}
}