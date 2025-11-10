package com.shoppr.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.shoppr.model.Post;

class PostClusterItem implements com.google.maps.android.clustering.ClusterItem {
	private final LatLng position;
	private final String title;
	private final String snippet;
	public final Post post;

	public PostClusterItem(double lat, double lng, String title, String snippet, Post post) {
		this.position = new LatLng(lat, lng);
		this.title = title;
		this.snippet = snippet;
		this.post = post;
	}

	@NonNull
	@Override
	public LatLng getPosition() {
		return position;
	}

	@Nullable
	@Override
	public String getTitle() {
		return title;
	}

	@Nullable
	@Override
	public String getSnippet() {
		return snippet;
	}

	@Nullable
	public Float getZIndex() { return 0.0f; }
}
