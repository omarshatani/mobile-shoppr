package com.shoppr.checkout;


import com.shoppr.model.Post;
import com.shoppr.model.Request;
import com.shoppr.model.User;

// This class holds all the data needed to render the checkout screen.
public class CheckoutState {

	private final Post post;
	private final Request request;
	private final User seller;
	private final double serviceFee = 5.00; // Example service fee

	public CheckoutState(Post post, Request request, User seller) {
		this.post = post;
		this.request = request;
		this.seller = seller;
	}

	public Post getPost() {
		return post;
	}

	public Request getRequest() {
		return request;
	}

	public User getSeller() {
		return seller;
	}

	public double getServiceFee() {
		return serviceFee;
	}

	public double getTotalAmount() {
		return request.getOfferAmount() + serviceFee;
	}
}