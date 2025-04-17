package com.shoppr.model;

import java.util.Date;

public class Request {
	private String buyerId;
	private String sellerId;
	private Date creationDate;
	private String offer;
	private RequestStatus status;

	public Request () {}

	private Request (Builder builder) {
		this.buyerId = builder.buyerId;
		this.sellerId = builder.sellerId;
		this.creationDate = builder.creationDate;
		this.offer = builder.offer;
		this.status = builder.status;
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

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getOffer() {
		return offer;
	}

	public void setOffer(String offer) {
		this.offer = offer;
	}

	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}

	static class Builder {
		private String buyerId;
		private String sellerId;
		private Date creationDate;
		private String offer;
		private RequestStatus status;

		public Builder buyerId (String buyerId) {
			this.buyerId = buyerId;
			return this;
		}

		public Builder sellerId (String sellerId) {
			this.sellerId = sellerId;
			return this;
		}

		public Builder creationDate (Date creationDate) {
			this.creationDate = creationDate;
			return this;
		}

		public Builder offer (String offer) {
			this.offer = offer;
			return this;
		}

		public Builder status (RequestStatus status) {
			this.status = status;
			return this;

		}

		public Request build () {
			return new Request (this);
		}
	}
}
