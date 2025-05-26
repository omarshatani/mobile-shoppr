package com.shoppr.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Post {
	private String id;
	private String title;
	private String description;
	private String price;
	private ListingState state;
	private ListingType type;
	private List<String> imageUrl; // Changed from String[] to List<String>
	private String category;
	private User lister;
	private List<String> requests; // Changed from String[] to List<String>

	// Location fields
	@Nullable
	private Double latitude;
	@Nullable
	private Double longitude;
	@Nullable
	private String postAddress;

	public Post() {
		// Required empty public constructor for Firestore deserialization
		this.imageUrl = new ArrayList<>(); // Initialize lists
		this.requests = new ArrayList<>(); // Initialize lists
	}

	private Post(Builder builder) {
		this.id = builder.id;
		this.title = builder.title;
		this.description = builder.description;
		this.price = builder.price;
		this.state = builder.state;
		this.type = builder.type;
		this.imageUrl = builder.imageUrl != null ? new ArrayList<>(builder.imageUrl) : new ArrayList<>(); // Ensure new list
		this.category = builder.category;
		this.lister = builder.lister;
		this.requests = builder.requests != null ? new ArrayList<>(builder.requests) : new ArrayList<>(); // Ensure new list
		this.latitude = builder.latitude;
		this.longitude = builder.longitude;
		this.postAddress = builder.postAddress;
	}

	// Getters and Setters
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
	} // Return type changed

	public void setImageUrl(List<String> imageUrl) {
		this.imageUrl = imageUrl;
	} // Parameter type changed

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

	public List<String> getRequests() {
		return requests;
	} // Return type changed

	public void setRequests(List<String> requests) {
		this.requests = requests;
	} // Parameter type changed

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
		@Nullable
		private String id;
		private String title;
		private String description;
		private String price;
		private ListingState state;
		private ListingType type;
		private List<String> imageUrl = new ArrayList<>(); // Changed to List, initialized
		private String category;
		private User lister;
		private List<String> requests = new ArrayList<>(); // Changed to List, initialized
		@Nullable
		private Double latitude;
		@Nullable
		private Double longitude;
		@Nullable
		private String postAddress;

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
		} // Parameter type changed

		public Builder category(String category) {
			this.category = category;
			return this;
		}

		public Builder lister(User lister) {
			this.lister = lister;
			return this;
		}

		public Builder requests(List<String> requests) {
			this.requests = requests;
			return this;
		} // Parameter type changed

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