package com.shoppr.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.shoppr.domain.datasource.FirestorePostDataSource;
import com.shoppr.domain.repository.PostRepository;
import com.shoppr.model.Post;

import javax.inject.Inject;

public class PostRepositoryImpl implements PostRepository {
	private static final String TAG = "PostRepositoryImpl";
	private final FirestorePostDataSource postDataSource;

	@Inject
	public PostRepositoryImpl(FirestorePostDataSource postDataSource) {
		this.postDataSource = postDataSource;
	}

	@Override
	public void savePost(@NonNull Post post, @NonNull final SavePostCallbacks callbacks) {
		Log.d(TAG, "Saving post: " + post.getTitle());
		// Here you would add any necessary fields before saving, e.g.,
		// if post.getId() is null, generate one.
		// if post.getCreatedAt() is null, set it to ServerTimestamp.
		// For simplicity, assuming Post object is ready.

		postDataSource.savePost(post, new FirestorePostDataSource.FirestorePostCallbacks() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "Post saved successfully via DataSource.");
				callbacks.onSaveSuccess();
			}

			@Override
			public void onError(@NonNull String message) {
				Log.e(TAG, "Error saving post via DataSource: " + message);
				callbacks.onSaveError(message);
			}
		});
	}

	@Override
	public void getPostById(@NonNull String postId, @NonNull GetPostCallbacks callbacks) {
		// TODO: Implement
	}

	@Override
	public void getPostsByUser(@NonNull String userId, @NonNull GetPostsCallbacks callbacks) {
		// TODO: Implement
	}

	@Override
	public void getAllPosts(@NonNull GetPostsCallbacks callbacks) {
		// TODO: Implement
	}

	@Override
	public void deletePost(@NonNull String postId, @NonNull DeletePostCallbacks callbacks) {
		// TODO: Implement
	}
}
