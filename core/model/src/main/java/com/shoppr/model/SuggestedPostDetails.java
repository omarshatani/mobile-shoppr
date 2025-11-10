package com.shoppr.model;

import java.util.List;

public class SuggestedPostDetails {
	private ListingType listingType;
	private String title;
	private String description;
	private String extractedItemName;
	private Double price;
	private String currency;
	private List<String> categories;

	public SuggestedPostDetails() {
		// Default constructor
	}

	// This is the corrected constructor
	public SuggestedPostDetails(ListingType listingType, String title, String description, String extractedItemName, Double price, String currency, List<String> categories) {
		this.listingType = listingType;
		this.title = title;
		this.description = description;
		this.extractedItemName = extractedItemName;
		this.price = price;
		this.currency = currency;
		this.categories = categories;
	}

	// --- Getters and Setters ---

	public ListingType getListingType() {
		return listingType;
	}

	public void setListingType(ListingType listingType) {
		this.listingType = listingType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExtractedItemName() {
		return extractedItemName;
	}

	public void setExtractedItemName(String extractedItemName) {
		this.extractedItemName = extractedItemName;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
}