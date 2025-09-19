package com.shoppr.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable {
	private String id;
	private String name;
	private String email;
	private String phoneNumber;
	private String address;
	private List<String> favoritePosts;
	private double averageRating = 0.0;
	private int ratingCount = 0;

	@Nullable
	private Double latitude;
	@Nullable
	private Double longitude;
	@Nullable
	private String locationAddress;

	public User() {
	}

	private User(Builder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.email = builder.email;
		this.phoneNumber = builder.phoneNumber;
		this.address = builder.address;
		this.latitude = builder.latitude;
		this.longitude = builder.longitude;
		this.locationAddress = builder.locationAddress;
		this.favoritePosts = builder.favoritePosts != null ? new ArrayList<>(builder.favoritePosts) : new ArrayList<>();
		this.averageRating = builder.averageRating;
		this.ratingCount = builder.ratingCount;
	}

	protected User(Parcel in) {
		id = in.readString();
		name = in.readString();
		email = in.readString();
		phoneNumber = in.readString();
		address = in.readString();
		favoritePosts = in.createStringArrayList();
		averageRating = in.readDouble();
		ratingCount = in.readInt();
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
		locationAddress = in.readString();
	}

	public static final Creator<User> CREATOR = new Creator<User>() {
		@Override
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
	};

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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
	public String getLocationAddress() {
		return locationAddress;
	}

	public void setLocationAddress(@Nullable String locationAddress) {
		this.locationAddress = locationAddress;
	}

	public List<String> getFavoritePosts() {
		return favoritePosts;
	}

	public void setFavoritePosts(List<String> favoritePosts) {
		this.favoritePosts = favoritePosts;
	}

	public double getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(double averageRating) {
		this.averageRating = averageRating;
	}

	public int getRatingCount() {
		return ratingCount;
	}

	public void setRatingCount(int ratingCount) {
		this.ratingCount = ratingCount;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(@NonNull Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(email);
		dest.writeString(phoneNumber);
		dest.writeString(address);
		dest.writeStringList(favoritePosts);
		dest.writeDouble(averageRating);
		dest.writeInt(ratingCount);
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
		dest.writeString(locationAddress);
	}

	public static class Builder {
		private String id;
		private String name;
		private String email;
		private String phoneNumber;
		private String address;
		private List<String> favoritePosts = new ArrayList<>();
		private double averageRating = 0.0;
		private int ratingCount = 0;
		@Nullable
		private Double latitude;
		@Nullable
		private Double longitude;
		@Nullable
		private String locationAddress;

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder phoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
			return this;
		}

		public Builder address(String address) {
			this.address = address;
			return this;
		}

		public Builder favoritePosts(List<String> favoritePosts) {
			this.favoritePosts = favoritePosts;
			return this;
		}

		public Builder averageRating(double averageRating) {
			this.averageRating = averageRating;
			return this;
		}

		public Builder ratingCount(int ratingCount) {
			this.ratingCount = ratingCount;
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

		public Builder locationAddress(@Nullable String address) {
			this.locationAddress = address;
			return this;
		}

		public User build() {
			return new User(this);
		}
	}
}