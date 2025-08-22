package com.shoppr.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Request {

	private String id;
	private String postId;
	private String buyerId;
	private String sellerId;
	private Double offerAmount;
	private String offerCurrency;
	private String message;
	private RequestStatus status;

	@ServerTimestamp
	private Date createdAt;

	public Request() {
	}

	private Request(Builder builder) {
		this.id = builder.id;
		this.postId = builder.postId;
		this.buyerId = builder.buyerId;
		this.sellerId = builder.sellerId;
		this.offerAmount = builder.offerAmount;
		this.offerCurrency = builder.offerCurrency;
		this.message = builder.message;
		this.status = builder.status;
		this.createdAt = builder.createdAt;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getBuyerId() {
		return buyerId;
	}

	public void setBuyerId(String buyerId) {
		this.buyerId = buyerId;
	}

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public Double getOfferAmount() {
		return offerAmount;
	}

	public void setOfferAmount(Double offerAmount) {
		this.offerAmount = offerAmount;
	}

	public String getOfferCurrency() {
		return offerCurrency;
	}

	public void setOfferCurrency(String offerCurrency) {
		this.offerCurrency = offerCurrency;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public static class Builder {
		private String id;
		private String postId;
		private String buyerId;
		private String sellerId;
		private Double offerAmount;
		private String offerCurrency;
		private String message;
		private RequestStatus status;
		private Date createdAt;

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder postId(String postId) {
			this.postId = postId;
			return this;
		}

		public Builder buyerId(String buyerId) {
			this.buyerId = buyerId;
			return this;
		}

		public Builder sellerId(String sellerId) {
			this.sellerId = sellerId;
			return this;
		}

		public Builder offerAmount(Double offerAmount) {
			this.offerAmount = offerAmount;
			return this;
		}

		public Builder offerCurrency(String offerCurrency) {
			this.offerCurrency = offerCurrency;
			return this;
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public Builder status(RequestStatus status) {
			this.status = status;
			return this;
		}

		public Builder createdAt(Date createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Request build() {
			return new Request(this);
		}
	}
}