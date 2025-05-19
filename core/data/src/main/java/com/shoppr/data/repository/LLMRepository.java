package com.shoppr.data.repository;

import androidx.annotation.NonNull;

import com.shoppr.model.SuggestedPostDetails;

public interface LLMRepository { // Or a more generic CloudFunctionRepository
    interface LLMAnalysisCallbacks {
        void onSuccess(@NonNull SuggestedPostDetails suggestions); // Returns domain model
        void onError(@NonNull String message);
    }
    void analyzeTextForPost(@NonNull String text, @NonNull LLMAnalysisCallbacks callbacks);
 }