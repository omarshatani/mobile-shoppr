package com.shoppr.data.datasource;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.shoppr.domain.datasource.FirebaseFunctionsDataSource;
import com.shoppr.model.ListingType;
import com.shoppr.model.SuggestedPostDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseFunctionsDataSourceImpl implements FirebaseFunctionsDataSource {
	private static final String TAG = "FunctionsDataSourceImpl";
	private static final String CLOUD_FUNCTION_NAME = "generatePostSuggestions";
	private final FirebaseFunctions functions;

	@Inject
	public FirebaseFunctionsDataSourceImpl(FirebaseFunctions functions) {
		this.functions = functions;
	}

	@Override
	public void getPostSuggestions(
			@NonNull String text,
			@Nullable String baseOfferPrice,
			@Nullable String baseOfferCurrency,
			@NonNull LLMCallbacks callbacks
	) {
		Map<String, Object> data = new HashMap<>();
		data.put("text", text);
		if (baseOfferPrice != null && !baseOfferPrice.trim().isEmpty()) {
			data.put("baseOfferPrice", baseOfferPrice);
		}
		if (baseOfferCurrency != null && !baseOfferCurrency.isEmpty()) {
			data.put("baseOfferCurrency", baseOfferCurrency);
		}

		functions.getHttpsCallable(CLOUD_FUNCTION_NAME)
				.call(data)
				.addOnCompleteListener(task -> {
					if (task.isSuccessful() && task.getResult() != null) {
						try {
							Map<String, Object> resultData = (Map<String, Object>) task.getResult().getData();
							if (Boolean.TRUE.equals(resultData.get("success"))) {
								Map<String, Object> suggestionsMap = (Map<String, Object>) resultData.get("data");
								// Here you would map the map to your SuggestedPostDetails object
								// For simplicity, I'm assuming a direct mapping for now.
								SuggestedPostDetails details = mapToSuggestedPostDetails(suggestionsMap);
								callbacks.onSuccess(details);
							} else {
								String error = (String) resultData.getOrDefault("error", "Unknown error from function.");
								callbacks.onError(error);
							}
						} catch (Exception e) {
							callbacks.onError("Error parsing response: " + e.getMessage());
						}
					} else {
						Exception e = task.getException();
						String errorMessage = "Failed to connect to AI service.";
						if (e instanceof FirebaseFunctionsException) {
							errorMessage = "AI service error: " + e.getMessage();
						} else if (e != null) {
							errorMessage = e.getMessage();
						}
						callbacks.onError(errorMessage);
					}
				});
	}

	@SuppressWarnings("unchecked")
	private SuggestedPostDetails mapToSuggestedPostDetails(Map<String, Object> map) {
		if (map == null) {
			return new SuggestedPostDetails();
		}

		String listingTypeStr = (String) map.get("listingType");
		ListingType listingTypeEnum = ListingType.UNKNOWN;
		if (listingTypeStr != null) {
			try {
				listingTypeEnum = ListingType.valueOf(listingTypeStr.toUpperCase().replace(" ", "_"));
			} catch (IllegalArgumentException e) {
				Log.w(TAG, "Invalid listingType string from LLM: '" + listingTypeStr + "'. Defaulting to UNKNOWN.", e);
			}
		}

		String title = (String) map.get("suggestedTitle");
		String description = (String) map.get("suggestedDescription");
		String itemName = (String) map.get("extractedItemName");
		String currency = (String) map.get("currency");

		List<String> categories = new ArrayList<>();
		if (map.get("suggestedCategories") instanceof List) {
			categories = (List<String>) map.get("suggestedCategories");
		}

		Double price = null;
		if (map.get("price") instanceof Number) {
			price = ((Number) map.get("price")).doubleValue();
		}

		return new SuggestedPostDetails(
				listingTypeEnum,
				title,
				description,
				itemName,
				price,
				currency,
				categories
		);
	}
}