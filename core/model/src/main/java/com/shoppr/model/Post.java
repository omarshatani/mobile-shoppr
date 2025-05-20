package com.shoppr.model;

public class Post {
	private String title;
	private String description;
	private String price;
	private ListingState state;
	private ListingType type;
	private String[] imageUrl;
	private String category;
	private User lister;
	private String[] requests;

	public Post() {
	}

	private Post(Builder builder) {
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

	static class Builder {
		private String title;
		private String description;
		private String price;
		private ListingState state;
		private ListingType type;
		private String[] imageUrl;
		private String category;
		private User lister;
		private String[] requests;

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

		public Post build() {
			return new Post(this);
		}

	}

}
