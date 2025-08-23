package com.shoppr.request;


import com.shoppr.model.Post;
import com.shoppr.model.Request;

// A simple data class to hold the combined data for our RecyclerView
public class RequestUiModel {

	private final Request request;
	private final Post post;

	public RequestUiModel(Request request, Post post) {
		this.request = request;
		this.post = post;
	}

	public Request getRequest() {
		return request;
	}

	public Post getPost() {
		return post;
	}
}