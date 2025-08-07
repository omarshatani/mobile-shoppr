package com.shoppr.data.repository;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.shoppr.domain.datasource.FirebaseStorageDataSource;
import com.shoppr.domain.datasource.FirestorePostDataSource;
import com.shoppr.domain.repository.PostRepository;
import com.shoppr.model.Post;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PostRepositoryImpl implements PostRepository {

	private final FirestorePostDataSource firestorePostDataSource;
	private final FirebaseStorageDataSource firebaseStorageDataSource;

	@Inject
	public PostRepositoryImpl(
			FirestorePostDataSource firestorePostDataSource,
			FirebaseStorageDataSource firebaseStorageDataSource
	) {
		this.firestorePostDataSource = firestorePostDataSource;
		this.firebaseStorageDataSource = firebaseStorageDataSource;
	}

	@Override
	public LiveData<List<Post>> getFeedPosts(@Nullable String currentUserIdToExclude) {
		return firestorePostDataSource.getFeedPosts(currentUserIdToExclude);
	}

	@Override
	public LiveData<List<Post>> getPostsForUser(@NonNull String userId) {
		return firestorePostDataSource.getPostsForUser(userId);
	}

	@Override
	public LiveData<List<Post>> getPostsByIds(@NonNull List<String> postIds) {
		return firestorePostDataSource.getPostsByIds(postIds);
	}

	@Override
	public void getPostById(@NonNull String postId, @NonNull PostCallbacks callbacks) {
		firestorePostDataSource.getPostById(postId, new FirestorePostDataSource.PostOperationCallbacks() {
			@Override
			public void onSuccess(@NonNull Post post) {
				callbacks.onSuccess(post);
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}

			@Override
			public void onNotFound() {
				callbacks.onNotFound();
			}
		});
	}

	@Override
	public void createPost(Post post, List<Uri> imageUris, PostCreationCallbacks callback) {
		firebaseStorageDataSource.uploadImages(imageUris, new FirebaseStorageDataSource.UploadCallbacks() {
			@Override
			public void onSuccess(@NonNull List<String> imageUrls) {
				post.setImageUrl(imageUrls);
				firestorePostDataSource.createPost(post, new FirestorePostDataSource.PostOperationCallbacks() {
					@Override
					public void onSuccess(@NonNull Post createdPost) {
						callback.onSuccess(createdPost);
					}

					@Override
					public void onError(@NonNull String message) {
						callback.onError(message);
					}

					@Override
					public void onNotFound() {
						// This case is not applicable for create
					}
				});
			}

			@Override
			public void onError(@NonNull String message) {
				callback.onError(message);
			}
		});
	}
}