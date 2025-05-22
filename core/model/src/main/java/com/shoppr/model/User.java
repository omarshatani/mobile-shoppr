package com.shoppr.model;

import androidx.annotation.Nullable;

public class User {
	private String id;
	private String name;
	private String email;
	private String phoneNumber;
	private String address; // General address string

	// New fields for last known/default posting location
	@Nullable
	private Double lastLatitude;
	@Nullable
	private Double lastLongitude;
	@Nullable
	private String lastLocationAddress; // Optional: A reverse-geocoded address for this lat/lon

	public User() {}

	private User(Builder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.email = builder.email;
		this.phoneNumber = builder.phoneNumber;
		this.address = builder.address;
		this.lastLatitude = builder.lastLatitude;
		this.lastLongitude = builder.lastLongitude;
		this.lastLocationAddress = builder.lastLocationAddress;
	}

	// Existing getters and setters...
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

	// Getters and Setters for new location fields
	@Nullable
	public Double getLastLatitude() {
		return lastLatitude;
	}

	public void setLastLatitude(@Nullable Double lastLatitude) {
		this.lastLatitude = lastLatitude;
	}

	@Nullable
	public Double getLastLongitude() {
		return lastLongitude;
	}

	public void setLastLongitude(@Nullable Double lastLongitude) {
		this.lastLongitude = lastLongitude;
	}

	@Nullable
	public String getLastLocationAddress() {
		return lastLocationAddress;
	}

	public void setLastLocationAddress(@Nullable String lastLocationAddress) {
		this.lastLocationAddress = lastLocationAddress;
	}


	public static class Builder {
		private String id;
		private String name;
		private String email;
		private String phoneNumber;
		private String address;
		@Nullable
		private Double lastLatitude;
		@Nullable
		private Double lastLongitude;
		@Nullable
		private String lastLocationAddress;

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

		public Builder lastLatitude(@Nullable Double latitude) {
			this.lastLatitude = latitude;
			return this;
		}

		public Builder lastLongitude(@Nullable Double longitude) {
			this.lastLongitude = longitude;
			return this;
		}

		public Builder lastLocationAddress(@Nullable String address) {
			this.lastLocationAddress = address;
			return this;
		}


		public User build() {
			return new User(this);
		}
	}
}
