package com.shoppr.domain.repository;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.shoppr.model.Post;

import java.util.List;

public interface PostRepository {

	LiveData<List<Post>> getFeedPosts(@Nullable String currentUserIdToExclude);

	LiveData<List<Post>> getPostsForUser(@NonNull String userId);

	LiveData<List<Post>> getPostsByIds(@NonNull List<String> postIds);

	interface PostCallbacks {
		void onSuccess(@NonNull Post post);
		void onError(@NonNull String message);
		void onNotFound();
	}

	void getPostById(@NonNull String postId, @NonNull PostCallbacks callbacks);

	interface PostCreationCallbacks {
		void onSuccess(@NonNull Post createdPost);

		void onError(String message);
	}

	void createPost(Post post, List<Uri> imageUris, PostCreationCallbacks callback);
}