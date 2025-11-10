package com.shoppr.domain.usecase;

import androidx.lifecycle.LiveData;

import com.shoppr.model.Post;

import java.util.List;

public interface GetMapPostsUseCase {
	LiveData<List<Post>> execute(String currentUserIdToExclude);
}