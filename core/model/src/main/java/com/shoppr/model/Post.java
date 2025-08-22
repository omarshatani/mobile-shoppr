package com.shoppr.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post implements Parcelable {
	private String id;
	private String title;
	private String description;
	private String price;
	private String currency;
	private ListingState state;
	private ListingType type;
	private List<String> imageUrl;
	private List<String> categories;
	private User lister;
	private List<String> requests;
	private List<String> offeringUserIds;

	@Nullable
	private Double latitude;
	@Nullable
	private Double longitude;
	@Nullable
	private String postAddress;

	@ServerTimestamp
	private Date createdAt;
	@ServerTimestamp
	private Date updatedAt;

	public Post() {
		this.imageUrl = new ArrayList<>();
		this.requests = new ArrayList<>();
		this.categories = new ArrayList<>();
		this.offeringUserIds = new ArrayList<>();
	}

	private Post(Builder builder) {
		this.id = builder.id;
		this.title = builder.title;
		this.description = builder.description;
		this.price = builder.price;
		this.currency = builder.currency;
		this.state = builder.state;
		this.type = builder.type;
		this.imageUrl = builder.imageUrl != null ? new ArrayList<>(builder.imageUrl) : new ArrayList<>();
		this.categories = builder.categories != null ? new ArrayList<>(builder.categories) : new ArrayList<>(); // Changed to handle List
		this.lister = builder.lister;
		this.requests = builder.requests != null ? new ArrayList<>(builder.requests) : new ArrayList<>();
		this.latitude = builder.latitude;
		this.longitude = builder.longitude;
		this.postAddress = builder.postAddress;
		this.createdAt = builder.createdAt;
		this.updatedAt = builder.updatedAt;
	}

	protected Post(Parcel in) {
		id = in.readString();
		title = in.readString();
		description = in.readString();
		price = in.readString();
		currency = in.readString();
		imageUrl = in.createStringArrayList();
		categories = in.createStringArrayList();
		requests = in.createStringArrayList();
		offeringUserIds = in.createStringArrayList();
		if (in.readByte() == 0) {
			latitude = null;
		} else {
			latitude = in.readDouble();
		}
		if (in.readByte() == 0) {
			longitude = null;
		} else {
			longitude = in.readDouble();
		}
		postAddress = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(title);
		dest.writeString(description);
		dest.writeString(price);
		dest.writeString(currency);
		dest.writeStringList(imageUrl);
		dest.writeStringList(categories);
		dest.writeStringList(requests);
		dest.writeStringList(offeringUserIds);
		if (latitude == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeDouble(latitude);
		}
		if (longitude == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeDouble(longitude);
		}
		dest.writeString(postAddress);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<Post> CREATOR = new Creator<Post>() {
		@Override
		public Post createFromParcel(Parcel in) {
			return new Post(in);
		}

		@Override
		public Post[] newArray(int size) {
			return new Post[size];
		}
	};

	@Nullable
	public String getId() {
		return id;
	}

	public void setId(@Nullable String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public ListingState getState() {
		return state;
	}

	public void setState(ListingState state) {
		this.state = state;
	}

	public ListingType getType() {
		return type;
	}

	public void setType(ListingType type) {
		this.type = type;
	}

	public List<String> getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(List<String> imageUrl) {
		this.imageUrl = imageUrl;
	}

	public List<String> getCategories() { // Updated getter
		return categories;
	}

	public void setCategories(List<String> categories) { // Updated setter
		this.categories = categories;
	}

	public User getLister() {
		return lister;
	}

	public void setLister(User lister) {
		this.lister = lister;
	}

	public List<String> getRequests() {
		return requests;
	}

	public void setRequests(List<String> requests) {
		this.requests = requests;
	}

	public List<String> getOfferingUserIds() {
		return offeringUserIds;
	}

	public void setOfferingUserIds(List<String> offeringUserIds) {
		this.offeringUserIds = offeringUserIds;
	}

	@Nullable
	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(@Nullable Double latitude) {
		this.latitude = latitude;
	}

	@Nullable
	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(@Nullable Double longitude) {
		this.longitude = longitude;
	}

	@Nullable
	public String getPostAddress() {
		return postAddress;
	}

	public void setPostAddress(@Nullable String postAddress) {
		this.postAddress = postAddress;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public static class Builder {
		@Nullable
		private String id;
		private String title;
		private String description;
		private String price;
		private String currency;
		private ListingState state;
		private ListingType type;
		private List<String> imageUrl = new ArrayList<>();
		private List<String> categories = new ArrayList<>(); // Changed to List<String>
		private User lister;
		private List<String> requests = new ArrayList<>();
		@Nullable
		private Double latitude;
		@Nullable
		private Double longitude;
		@Nullable
		private String postAddress;
		private Date createdAt;
		private Date updatedAt;

		public Builder id(@Nullable String id) {
			this.id = id;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder price(String price) {
			this.price = price;
			return this;
		}

		public Builder currency(String currency) {
			this.currency = currency;
			return this;
		}

		public Builder state(ListingState state) {
			this.state = state;
			return this;
		}

		public Builder type(ListingType type) {
			this.type = type;
			return this;
		}

		public Builder imageUrl(List<String> imageUrl) {
			this.imageUrl = imageUrl;
			return this;
		}

		public Builder categories(List<String> categories) {
			this.categories = categories;
			return this;
		}

		public Builder lister(User lister) {
			this.lister = lister;
			return this;
		}

		public Builder requests(List<String> requests) {
			this.requests = requests;
			return this;
		}

		public Builder latitude(@Nullable Double latitude) {
			this.latitude = latitude;
			return this;
		}

		public Builder longitude(@Nullable Double longitude) {
			this.longitude = longitude;
			return this;
		}

		public Builder postAddress(@Nullable String postAddress) {
			this.postAddress = postAddress;
			return this;
		}

		public Builder createdAt(Date createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Builder updatedAt(Date updatedAt) {
			this.updatedAt = updatedAt;
			return this;
		}

		public Post build() {
			return new Post(this);
		}
	}
}