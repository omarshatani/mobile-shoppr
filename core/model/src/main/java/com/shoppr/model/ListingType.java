package com.shoppr.model;

public enum ListingType {
	BUY("buy"),
	SELL("sell");

	final String label;

	private ListingType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
