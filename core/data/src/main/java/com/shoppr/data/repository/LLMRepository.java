package com.shoppr.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.model.SuggestedPostDetails;

public interface LLMRepository {
    interface LLMAnalysisCallbacks {
        void onSuccess(@NonNull SuggestedPostDetails suggestions);
        void onError(@NonNull String message);
    }

    void analyzeTextForPost(
            @NonNull String text,
            @Nullable Double baseOfferPrice,
            @Nullable String baseOfferCurrency,
            @NonNull LLMAnalysisCallbacks callbacks
    );
}