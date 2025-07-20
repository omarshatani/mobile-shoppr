package com.shoppr.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents the structured suggestions extracted by the LLM
 * for creating a new post. This is a domain model.
 */
public class SuggestedPostDetails {
	@NonNull
	public final ListingType listingType;
	@NonNull
	public final String suggestedTitle;
	@NonNull
	public final String suggestedDescription;
	@NonNull
	public final String extractedItemName;
	@NonNull
	public final String extractedCurrency;
	@Nullable
	public final String suggestedCategory;

	public SuggestedPostDetails(
			@NonNull ListingType listingType,
			@NonNull String suggestedTitle,
			@NonNull String suggestedDescription,
			@NonNull String extractedItemName, @NonNull String extractedCurrency,
			@Nullable String suggestedCategory) {
		this.listingType = listingType;
		this.suggestedTitle = suggestedTitle;
		this.suggestedDescription = suggestedDescription;
		this.extractedItemName = extractedItemName;
		this.extractedCurrency = extractedCurrency;
		this.suggestedCategory = suggestedCategory;
	}

	// --- Getters ---
	@NonNull
	public ListingType getListingType() {
		return listingType;
	}

	@NonNull
	public String getSuggestedTitle() {
		return suggestedTitle;
	}

	@NonNull
	public String getSuggestedDescription() {
		return suggestedDescription;
	}

	@NonNull
	public String getExtractedItemName() {
		return extractedItemName;
	}

	@NonNull
	public String getExtractedCurrency() {
		return extractedCurrency;
	}

	@Nullable
	public String getSuggestedCategory() {
		return suggestedCategory;
	}

	@NonNull
	@Override
	public String toString() {
		return "SuggestedPostDetails{" +
				"listingType=" + listingType.name() +
				", suggestedTitle='" + suggestedTitle + '\'' +
				", suggestedDescription='" + suggestedDescription + '\'' +
				", extractedItemName='" + extractedItemName + '\'' +
				", extractedCurrency='" + extractedCurrency + '\'' +
				", suggestedCategory='" + suggestedCategory + '\'' +
				'}';
	}
}