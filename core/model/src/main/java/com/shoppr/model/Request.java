package com.shoppr.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Request implements Parcelable {

	private String id;
	private String postId;
	private String buyerId;
	private String sellerId;
	private Double offerAmount;
	private String offerCurrency;
	private String message;
	private RequestStatus status;
	private Date createdAt;
	private List<ActivityEntry> activityTimeline;

	public Request() {
		this.activityTimeline = new ArrayList<>();
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
		this.activityTimeline = builder.activityTimeline != null ? new ArrayList<>(builder.activityTimeline) : new ArrayList<>();
	}


	protected Request(Parcel in) {
		id = in.readString();
		postId = in.readString();
		buyerId = in.readString();
		sellerId = in.readString();
		if (in.readByte() == 0) {
			offerAmount = null;
		} else {
			offerAmount = in.readDouble();
		}
		offerCurrency = in.readString();
		message = in.readString();
		status = (RequestStatus) in.readSerializable();
		long tmpDate = in.readLong();
		createdAt = tmpDate == -1 ? null : new Date(tmpDate);
		// This correctly reads the list of ActivityEntry objects
		activityTimeline = in.createTypedArrayList(ActivityEntry.CREATOR);
	}

	public static final Creator<Request> CREATOR = new Creator<Request>() {
		@Override
		public Request createFromParcel(Parcel in) {
			return new Request(in);
		}

		@Override
		public Request[] newArray(int size) {
			return new Request[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(postId);
		dest.writeString(buyerId);
		dest.writeString(sellerId);
		if (offerAmount == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeDouble(offerAmount);
		}
		dest.writeString(offerCurrency);
		dest.writeString(message);
		dest.writeSerializable(status);
		dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
		dest.writeTypedList(activityTimeline);
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

	public List<ActivityEntry> getActivityTimeline() {
		return activityTimeline;
	}

	public void setActivityTimeline(List<ActivityEntry> activityTimeline) {
		this.activityTimeline = activityTimeline;
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
		private List<ActivityEntry> activityTimeline;

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

		public Builder activityTimeline(List<ActivityEntry> activityTimeline) {
			this.activityTimeline = activityTimeline;
			return this;
		}

		public Request build() {
			return new Request(this);
		}
	}
}