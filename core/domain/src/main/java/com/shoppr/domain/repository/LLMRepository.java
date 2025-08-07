package com.shoppr.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.model.SuggestedPostDetails;

import java.util.List;

public interface LLMRepository {
    interface LLMAnalysisCallbacks {
        void onSuccess(@NonNull SuggestedPostDetails suggestions);
        void onError(@NonNull String message);
    }

    void getPostSuggestionsFromLLM(
        @NonNull String text,
        @Nullable List<String> imageUrls,
        @Nullable String baseOfferPrice,
        @Nullable String baseOfferCurrency,
        @NonNull LLMAnalysisCallbacks callbacks
    );
 }