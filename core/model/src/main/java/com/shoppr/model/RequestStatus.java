package com.shoppr.model;

public enum RequestStatus {
	SELLER_PENDING,
	BUYER_PENDING,
	SELLER_ACCEPTED, // New: Buyer has accepted, awaiting seller's final confirmation
	BUYER_ACCEPTED,  // New: Seller has accepted, awaiting buyer's final confirmation to pay
	COMPLETED,
	REJECTED
}