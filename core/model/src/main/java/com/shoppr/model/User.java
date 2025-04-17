package com.shoppr.model;

public class User {
	private String id;
	private String name;
	private String email;
	private String phoneNumber;
	private String address;

	public User() {}

	private User(Builder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.email = builder.email;
		this.phoneNumber = builder.phoneNumber;
		this.address = builder.address;
	}

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

	static class Builder {
		private String id;
		private String name;
		private String email;
		private String phoneNumber;
		private String address;

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

		public User build() {
			return new User(this);
		}

	}

}
