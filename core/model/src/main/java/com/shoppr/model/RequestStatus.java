package com.shoppr.model;

public enum RequestStatus {
	SELLER_PENDING, // It's the seller's turn to respond
	BUYER_PENDING,  // It's the buyer's turn to respond
	ACCEPTED,       // An offer was accepted, awaiting buyer confirmation
	COMPLETED,      // The deal is done
	REJECTED        // The deal is off
}