package com.shoppr.model;

import androidx.annotation.Nullable;

public class Post {
	private String id;
	private String title;
	private String description;
	private String price;
	private ListingState state;
	private ListingType type;
	private String[] imageUrl;
	private String category;
	private User lister;
	private String[] requests;

	// New location fields for the Post itself
	@Nullable
	private Double latitude;
	@Nullable
	private Double longitude;
	@Nullable
	private String postAddress; // Specific address for this post/item

	public Post() {
	}

	private Post(Builder builder) {
		this.id = builder.id;
		this.title = builder.title;
		this.description = builder.description;
		this.price = builder.price;
		this.state = builder.state;
		this.type = builder.type;
		this.imageUrl = builder.imageUrl;
		this.category = builder.category;
		this.lister = builder.lister;
		this.requests = builder.requests;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	public String[] getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String[] imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public User getLister() {
		return lister;
	}

	public void setLister(User lister) {
		this.lister = lister;
	}

	public String[] getRequests() {
		return requests;
	}

	public void setRequests(String[] requests) {
		this.requests = requests;
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

	public static class Builder {
		private String id;
		private String title;
		private String description;
		private String price;
		private ListingState state;
		private ListingType type;
		private String[] imageUrl;
		private String category;
		private User lister;
		private String[] requests;
		@Nullable
		private Double latitude;
		@Nullable
		private Double longitude;
		@Nullable
		private String postAddress;

		public Builder id(String id) {
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

		public Builder state(ListingState state) {
			this.state = state;
			return this;
		}

		public Builder type(ListingType type) {
			this.type = type;
			return this;
		}

		public Builder imageUrl(String[] imageUrl) {
			this.imageUrl = imageUrl;
			return this;
		}

		public Builder category(String category) {
			this.category = category;
			return this;
		}

		public Builder lister(User lister) {
			this.lister = lister;
			return this;
		}

		public Builder requests(String[] requests) {
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

		public Post build() {
			return new Post(this);
		}

	}

}
