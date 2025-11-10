package com.shoppr.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Transaction {

	private String id;
	private String requestId;
	private String postId;
	private String buyerId;
	private String sellerId;
	private double amount;
	private String currency;
	private double serviceFee;
	private double totalAmount;
	private PaymentMethod paymentMethod;
	private TransactionStatus status;
	@ServerTimestamp
	private Date createdAt;

	public Transaction() {
	} // Required empty constructor for Firestore

	private Transaction(Builder builder) {
		this.id = builder.id;
		this.requestId = builder.requestId;
		this.postId = builder.postId;
		this.buyerId = builder.buyerId;
		this.sellerId = builder.sellerId;
		this.amount = builder.amount;
		this.currency = builder.currency;
		this.serviceFee = builder.serviceFee;
		this.totalAmount = builder.totalAmount;
		this.paymentMethod = builder.paymentMethod;
		this.status = builder.status;
		this.createdAt = builder.createdAt;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
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

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public double getServiceFee() {
		return serviceFee;
	}

	public void setServiceFee(double serviceFee) {
		this.serviceFee = serviceFee;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
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
		private String requestId;
		private String postId;
		private String buyerId;
		private String sellerId;
		private double amount;
		private String currency;
		private double serviceFee;
		private double totalAmount;
		private PaymentMethod paymentMethod;
		private TransactionStatus status;
		private Date createdAt;

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder requestId(String requestId) {
			this.requestId = requestId;
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

		public Builder amount(double amount) {
			this.amount = amount;
			return this;
		}

		public Builder currency(String currency) {
			this.currency = currency;
			return this;
		}

		public Builder serviceFee(double serviceFee) {
			this.serviceFee = serviceFee;
			return this;
		}

		public Builder totalAmount(double totalAmount) {
			this.totalAmount = totalAmount;
			return this;
		}

		public Builder paymentMethod(PaymentMethod paymentMethod) {
			this.paymentMethod = paymentMethod;
			return this;
		}

		public Builder status(TransactionStatus status) {
			this.status = status;
			return this;
		}

		public Builder createdAt(Date createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Transaction build() {
			return new Transaction(this);
		}
	}
}