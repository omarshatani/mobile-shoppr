package com.shoppr.domain.usecase;

import androidx.lifecycle.LiveData;

import com.shoppr.model.Post;

import java.util.List;

public interface GetFavoritePostsUseCase {
	/**
	 * Executes the use case and returns a LiveData object
	 * that will emit the list of the user's favorite posts.
	 * The list will automatically update if the user's favorites change.
	 *
	 * @return A LiveData object holding the list of favorite posts.
	 */
	LiveData<List<Post>> execute();
}