package com.shoppr.domain.usecase;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.shoppr.model.Post;

import java.util.List;

public interface GetMapPostsUseCase {
	/**
	 * Executes the logic to get posts to be displayed on the map.
	 * @param currentUserId The ID of the currently logged-in user, to potentially exclude their posts.
	 * @return LiveData holding a list of Post objects.
	 */
	LiveData<List<Post>> execute(@Nullable String currentUserId);
}