package com.shoppr.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.shoppr.domain.repository.LLMRepository;
import com.shoppr.model.ListingType;
import com.shoppr.model.SuggestedPostDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LLMRepositoryImpl implements LLMRepository {
  private static final String TAG = "LLMRepositoryImpl";
  // Name of your Cloud Function that generates suggestions (and dynamically creates prompts)
  private static final String CLOUD_FUNCTION_NAME = "generatePostSuggestions";
  private final FirebaseFunctions functions;

  @Inject
  public LLMRepositoryImpl(FirebaseFunctions functions) { // FirebaseFunctions provided by Hilt
    this.functions = functions;
  }

  @Override
  public void getPostSuggestionsFromLLM(
      @NonNull String text,
      @Nullable List<String> imageUrls,
      @Nullable String baseOfferPrice,
      @Nullable String baseOfferCurrency,
      @NonNull final LLMAnalysisCallbacks callbacks) {

    Map<String, Object> data = new HashMap<>();
    data.put("text", text);
    if (imageUrls != null && !imageUrls.isEmpty()) {
      data.put("imageUrls", imageUrls); // Cloud function will decide if/how to use these for prompt generation
    }
    if (baseOfferPrice != null && !baseOfferPrice.trim().isEmpty()) {
      data.put("baseOfferPrice", baseOfferPrice);
    }
    if (baseOfferCurrency != null && !baseOfferCurrency.isEmpty()) {
      data.put("baseOfferCurrency", baseOfferCurrency);
    }

    Log.d(TAG, "Calling Firebase Cloud Function: " + CLOUD_FUNCTION_NAME + " with data: " + data);

    functions.getHttpsCallable(CLOUD_FUNCTION_NAME)
        .call(data)
        .addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            try {
              HttpsCallableResult result = task.getResult();
              if (result == null || result.getData() == null) {
                Log.e(TAG, "Cloud Function call successful but result or result.getData() is null.");
                callbacks.onError("AI service returned an empty response.");
                return;
              }
              Map<String, Object> resultData = (Map<String, Object>) result.getData();
              Log.d(TAG, "Cloud Function successful. Raw Data: " + resultData);

              if (Boolean.TRUE.equals(resultData.get("success"))) {
                Object suggestionsData = resultData.get("suggestions"); // Renamed from "data" to "suggestions" to match CF output
                if (suggestionsData instanceof Map) {
                  @SuppressWarnings("unchecked") // Safe cast after instanceof check
                  Map<String, Object> suggestionsMap = (Map<String, Object>) suggestionsData;
                  SuggestedPostDetails suggestions = mapToSuggestedPostDetails(suggestionsMap);
                  callbacks.onSuccess(suggestions);
                } else {
                  Log.e(TAG, "Cloud function returned success but 'suggestions' field is missing, null, or not a Map.");
                  callbacks.onError("AI service response format error (suggestions data missing or invalid).");
                }
              } else {
                String errorMessage = "Cloud function indicated failure.";
                if (resultData.get("error") instanceof String) {
                  errorMessage = (String) resultData.get("error");
                } else if (resultData.get("error") instanceof Map) {
                  // Deeper error object parsing if needed
                  Map<String, Object> errorMap = (Map<String, Object>) resultData.get("error");
                  errorMessage = (String) errorMap.getOrDefault("message", errorMessage);
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
              Log.e(TAG, "FirebaseFunctionsException details: " + ffe.getDetails());
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
    ListingType listingTypeEnum = ListingType.SELLING_ITEM; // Default or handle error
    if (listingTypeStr != null) {
      try {
        // Attempt to match the string from LLM to your enum constants
        listingTypeEnum = ListingType.valueOf(listingTypeStr.toUpperCase().replace(" ", "_"));
      } catch (IllegalArgumentException e) {
        Log.w(TAG, "Invalid listingType string from LLM: '" + listingTypeStr + "'. Defaulting to SELLING_ITEM.", e);
        // You might want to throw an error or handle this more gracefully
      }
    } else {
      Log.w(TAG, "listingType string from LLM is null. Defaulting to SELLING_ITEM.");
    }

    String title = (String) map.get("suggestedTitle");
    String description = (String) map.get("suggestedDescription");
    String itemName = (String) map.get("extractedItemName");
    String category = (String) map.get("suggestedCategory");

    return new SuggestedPostDetails(
        listingTypeEnum,
        title != null ? title : "Untitled Post", // Provide defaults for robustness
        description != null ? description : "No description generated.",
        itemName != null ? itemName : "N/A",
        category // Can be null
    );
  }
}