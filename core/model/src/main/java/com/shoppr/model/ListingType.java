package com.shoppr.model;

public enum ListingType {
	WANTING_TO_BUY_ITEM("buy"),
	SELLING_ITEM("sell"),
	WANTING_TO_OFFER_SERVICE("offer_service"),
	OFFER_TO_BUY_SERVICE("buy_service"),
	UNKNOWN("unknown");

	final String label;

	ListingType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
