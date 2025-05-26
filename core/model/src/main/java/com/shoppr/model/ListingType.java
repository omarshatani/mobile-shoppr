package com.shoppr.model;

public enum ListingType {
	WANTING_TO_BUY_ITEM("buy"),
	SELLING_ITEM("sell"),
	OFFERING_SERVICE("service"),
	REQUESTING_SERVICE("request"),
	UNKNOWN("unknown");

	final String label;

	private ListingType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
