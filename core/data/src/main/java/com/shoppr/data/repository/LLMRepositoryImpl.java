package com.shoppr.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.shoppr.model.ListingType;
import com.shoppr.model.SuggestedPostDetails;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class LLMRepositoryImpl implements LLMRepository {
    private static final String TAG = "LLMRepositoryImpl";
    private static final String CLOUD_FUNCTION_NAME = "analyzePostText"; // Name of your deployed function
    private final FirebaseFunctions functions;

    @Inject
    public LLMRepositoryImpl(FirebaseFunctions functions) { // FirebaseFunctions provided by Hilt
        this.functions = functions;
    }

    @Override
    public void analyzeTextForPost(
            @NonNull String text,
            @Nullable Double baseOfferPrice,
            @Nullable String baseOfferCurrency,
            @NonNull final LLMAnalysisCallbacks callbacks) {

        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        if (baseOfferPrice != null) {
            data.put("baseOfferPrice", baseOfferPrice);
        }
        if (baseOfferCurrency != null && !baseOfferCurrency.isEmpty()) {
            data.put("baseOfferCurrency", baseOfferCurrency);
        }

        Log.d(TAG, "Calling Firebase Cloud Function: " + CLOUD_FUNCTION_NAME + " with data: " + data.toString());

        functions.getHttpsCallable(CLOUD_FUNCTION_NAME)
            .call(data)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    try {
                        Map<String, Object> resultData = (Map<String, Object>) task.getResult().getData();
                        Log.d(TAG, "Cloud Function successful. Raw Data: " + resultData);

                        if (resultData != null && Boolean.TRUE.equals(resultData.get("success"))) {
                            Map<String, Object> suggestionsMap = (Map<String, Object>) resultData.get("data");
                            if (suggestionsMap != null) {
                                SuggestedPostDetails suggestions = mapToSuggestedPostDetails(suggestionsMap);
                                callbacks.onSuccess(suggestions);
                            } else {
                                Log.e(TAG, "Cloud function returned success but 'data' field is missing or null.");
                                callbacks.onError("AI service response format error (data missing).");
                            }
                        } else {
                            String errorMessage = "Cloud function indicated failure.";
                            if (resultData != null && resultData.get("error") instanceof Map) {
                                Map<String, Object> errorMap = (Map<String, Object>) resultData.get("error");
                                errorMessage = (String) errorMap.getOrDefault("message", errorMessage);
                            } else if (resultData != null && resultData.get("errorMessage") instanceof String) {
                                errorMessage = (String) resultData.get("errorMessage");
                            }
                            Log.e(TAG, "Cloud function failed or returned success:false. Message: " + errorMessage + " Full response: " + resultData);
                            callbacks.onError(errorMessage);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing successful cloud function response", e);
                        callbacks.onError("Error processing response from AI service: " + e.getMessage());
                    }
                } else {
                    Exception e = task.getException();
                    Log.e(TAG, "Cloud function call failed.", e);
                    String errorMessage = "Failed to connect to AI analysis service.";
                    if (e instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        errorMessage = "AI service error (" + ffe.getCode() + "): " + ffe.getMessage();
                    } else if (e != null) {
                        errorMessage = e.getMessage();
                    }
                    callbacks.onError(errorMessage);
                }
            });
    }

    // Helper to map from generic Map to your domain SuggestedPostDetails
    private SuggestedPostDetails mapToSuggestedPostDetails(Map<String, Object> map) {
        String listingTypeStr = (String) map.get("listingType");
        ListingType listingTypeEnum = ListingType.SELLING_ITEM; // Default
        if (listingTypeStr != null) {
            try {
                listingTypeEnum = ListingType.valueOf(listingTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Invalid listingType string from LLM: " + listingTypeStr + ". Defaulting.");
            }
        }

        String title = (String) map.get("suggestedTitle");
        String description = (String) map.get("suggestedDescription");
        String itemName = (String) map.get("extractedItemName");
        String category = (String) map.get("suggestedCategory");

        return new SuggestedPostDetails(
                listingTypeEnum,
                title != null ? title : "Untitled",
            description != null ? description : "",
                itemName != null ? itemName : "N/A",
            category
        );
    }
 }