package com.shoppr.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.shoppr.model.Post;

import java.util.List;

public interface PostRepository {
	interface SavePostCallbacks {
		void onSaveSuccess();

		void onSaveError(@NonNull String message);
	}

	interface GetPostCallbacks {
		void onPostLoaded(@NonNull Post post);

		void onDataNotAvailable(@NonNull String message);
	}

	interface GetPostsCallbacks {
		void onPostsLoaded(@NonNull Post[] posts);

		void onDataNotAvailable(@NonNull String message);
	}

	/**
	 * Saves a new post or updates an existing one.
	 *
	 * @param post      The Post object to save.
	 * @param callbacks Callbacks for success or error.
	 */
	void savePost(@NonNull Post post, @NonNull SavePostCallbacks callbacks);

	interface GetPostByIdCallbacks {
		void onSuccess(@NonNull Post post);

		void onError(@NonNull String message);

		void onNotFound();
	}

	/**
	 * Retrieves a post by its ID.
	 *
	 * @param postId    The ID of the post to retrieve.
	 * @param callbacks Callbacks for success or error.
	 */
	void getPostById(@NonNull String postId, @NonNull GetPostByIdCallbacks callbacks);

	/**
	 * Retrieves all posts created by a specific user.
	 *
	 * @param userId    The ID of the user.
	 * @param callbacks Callbacks for success or error.
	 */
	void getPostsByUser(@NonNull String userId, @NonNull GetPostsCallbacks callbacks);

	/**
	 * Retrieves all posts.
	 *
	 * @param callbacks Callbacks for success or error.
	 */
	void getAllPosts(@NonNull GetPostsCallbacks callbacks);

	interface DeletePostCallbacks {
		void onDeleteSuccess();

		void onDeleteError(@NonNull String message);
	}

	/**
	 * Deletes a post by its ID.
	 *
	 * @param postId The ID of the post to delete.
	 */
	void deletePost(@NonNull String postId, @NonNull DeletePostCallbacks callbacks);

	LiveData<List<Post>> getPostsForMap(@Nullable String currentUserId);
}
