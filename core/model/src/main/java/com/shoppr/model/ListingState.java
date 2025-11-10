package com.shoppr.model;

public enum ListingState {
	ACTIVE,     // For any post that is currently for sale
	COMPLETED,  // For a post that has been successfully sold
	CANCELLED   // A potential future state if a seller cancels a listing
}