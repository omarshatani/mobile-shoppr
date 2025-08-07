package com.shoppr.data.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.domain.repository.LLMRepository;
import com.shoppr.domain.usecase.GetLLMSuggestionsUseCase;
import com.shoppr.model.SuggestedPostDetails;

import java.util.List;

import javax.inject.Inject;

public class GetLLMSuggestionsUseCaseImpl implements GetLLMSuggestionsUseCase {

	private final LLMRepository llmRepository;

	@Inject
	public GetLLMSuggestionsUseCaseImpl(LLMRepository llmRepository) {
		this.llmRepository = llmRepository;
	}

	@Override
	public void execute(
			@NonNull String text,
			@Nullable List<String> imageUrls,
			@Nullable String baseOfferPrice,
			@Nullable String baseOfferCurrency,
			@NonNull LLMAnalysisCallbacks callbacks
	) {
		llmRepository.getPostSuggestionsFromLLM(
				text,
				imageUrls,
				baseOfferPrice,
				baseOfferCurrency,
				new LLMRepository.LLMAnalysisCallbacks() {
					@Override
					public void onSuccess(@NonNull SuggestedPostDetails suggestions) {
						callbacks.onSuccess(suggestions);
					}

					@Override
					public void onError(@NonNull String message) {
						callbacks.onError(message);
					}
				}
		);
	}
}