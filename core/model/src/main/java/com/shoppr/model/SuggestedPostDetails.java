package com.shoppr.model;

import androidx.annotation.Nullable;

public class SuggestedPostDetails {
	public final String listingType; // e.g., "SELLING_ITEM", "WANTING_TO_BUY_ITEM", etc.
	public final String title;
	public final String description;
	public final String itemName;
	public final Double price; // Using Double to allow for null
	public final String currency;
	public final String category;

	public SuggestedPostDetails(String listingType, String title, String description, String itemName, @Nullable Double price, @Nullable String currency, @Nullable String category) {
		this.listingType = listingType;
		this.title = title;
		this.description = description;
		this.itemName = itemName;
		this.price = price;
		this.currency = currency;
		this.category = category;
	}
	// Add getters if your User model uses private fields and getters

    public String getListingType() {
        return listingType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getItemName() {
        return itemName;
    }

    public Double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCategory() {
        return category;
    }
}