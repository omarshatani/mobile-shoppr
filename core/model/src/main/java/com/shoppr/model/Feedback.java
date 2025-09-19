package com.shoppr.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Feedback {
	private String id;
	private String transactionId;
	private String raterId;      // The user GIVING the rating
	private String rateeId;      // The user RECEIVING the rating
	private float rating;        // The star rating (e.g., 4.5)
	private String comment;
	@ServerTimestamp
	private Date createdAt;

	// Required empty constructor for Firestore
	public Feedback() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getRaterId() {
		return raterId;
	}

	public void setRaterId(String raterId) {
		this.raterId = raterId;
	}

	public String getRateeId() {
		return rateeId;
	}

	public void setRateeId(String rateeId) {
		this.rateeId = rateeId;
	}

	public float getRating() {
		return rating;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}