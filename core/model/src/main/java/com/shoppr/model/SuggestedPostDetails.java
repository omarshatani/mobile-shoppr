package com.shoppr.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents the structured suggestions extracted by the LLM
 * for creating a new post. This is a domain model.
 */
public class SuggestedPostDetails {
    @NonNull
    public final ListingType listingType; // e.g., "SELLING_ITEM", "WANTING_TO_BUY_ITEM", "OFFERING_SERVICE", "REQUESTING_SERVICE"
    @NonNull
    public final String suggestedTitle;
    @NonNull
    public final String suggestedDescription;
    @NonNull
    public final String extractedItemName; // The primary item/service identified
    @Nullable
    public final String suggestedCategory; // Optional

    // Note: Price and currency are handled separately as they come from user's direct input ("Base Offer")
    // and potentially as a secondary extraction from text by the LLM.
    // This model focuses on what the LLM primarily suggests based on text.

    public SuggestedPostDetails(
            @NonNull ListingType listingType,
            @NonNull String suggestedTitle,
            @NonNull String suggestedDescription,
            @NonNull String extractedItemName,
            @Nullable String suggestedCategory) {
        this.listingType = listingType;
        this.suggestedTitle = suggestedTitle;
        this.suggestedDescription = suggestedDescription;
        this.extractedItemName = extractedItemName;
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

    @Nullable
    public String getSuggestedCategory() {
        return suggestedCategory;
    }

    @NonNull
    @Override
    public String toString() {
        return "SuggestedPostDetails{" +
                "listingType='" + listingType + '\'' +
                ", suggestedTitle='" + suggestedTitle + '\'' +
                ", suggestedDescription='" + suggestedDescription + '\'' +
                ", extractedItemName='" + extractedItemName + '\'' +
                ", suggestedCategory='" + suggestedCategory + '\'' +
                '}';
    }
}