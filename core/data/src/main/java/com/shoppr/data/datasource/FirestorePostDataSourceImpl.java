package com.shoppr.data.datasource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.shoppr.domain.datasource.FirestorePostDataSource;
import com.shoppr.model.Post;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirestorePostDataSourceImpl implements FirestorePostDataSource {

	private final FirebaseFirestore firestore;
	private static final String POSTS_COLLECTION = "posts";

	@Inject
	public FirestorePostDataSourceImpl(FirebaseFirestore firestore) {
		this.firestore = firestore;
	}

	@Override
	public LiveData<List<Post>> getFeedPosts(@Nullable String currentUserIdToExclude) {
		MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();
		Query query = firestore.collection(POSTS_COLLECTION);

		if (currentUserIdToExclude != null && !currentUserIdToExclude.isEmpty()) {
			query = query.whereNotEqualTo("lister.id", currentUserIdToExclude);
		}

		query.addSnapshotListener((snapshots, e) -> {
			if (e != null) {
				postsLiveData.postValue(null);
				return;
			}
			List<Post> posts = new ArrayList<>();
			if (snapshots != null) {
				for (QueryDocumentSnapshot document : snapshots) {
					posts.add(document.toObject(Post.class));
				}
			}
			postsLiveData.postValue(posts);
		});
		return postsLiveData;
	}

	@Override
	public LiveData<List<Post>> getPostsForUser(@NonNull String userId) {
		MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();
		firestore.collection(POSTS_COLLECTION)
				.whereEqualTo("lister.id", userId)
				.orderBy("createdAt", Query.Direction.DESCENDING)
				.addSnapshotListener((snapshots, e) -> {
					if (e != null) {
						postsLiveData.postValue(null);
						return;
					}
					List<Post> posts = new ArrayList<>();
					if (snapshots != null) {
						for (QueryDocumentSnapshot document : snapshots) {
							posts.add(document.toObject(Post.class));
						}
					}
					postsLiveData.postValue(posts);
				});
		return postsLiveData;
	}

	@Override
	public LiveData<List<Post>> getPostsByIds(@NonNull List<String> postIds) {
		MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();
		if (postIds.isEmpty()) {
			postsLiveData.postValue(new ArrayList<>());
			return postsLiveData;
		}
		firestore.collection(POSTS_COLLECTION).whereIn("id", postIds)
				.addSnapshotListener((snapshots, e) -> {
					if (e != null) {
						postsLiveData.postValue(null);
						return;
					}
					List<Post> posts = new ArrayList<>();
					if (snapshots != null) {
						for (QueryDocumentSnapshot document : snapshots) {
							posts.add(document.toObject(Post.class));
						}
					}
					postsLiveData.postValue(posts);
				});
		return postsLiveData;
	}

	@Override
	public void getPostById(@NonNull String postId, @NonNull PostOperationCallbacks callbacks) {
		firestore.collection(POSTS_COLLECTION).document(postId).get()
				.addOnSuccessListener(documentSnapshot -> {
					if (documentSnapshot.exists()) {
						Post post = documentSnapshot.toObject(Post.class);
						callbacks.onSuccess(post);
					} else {
						callbacks.onNotFound();
					}
				})
				.addOnFailureListener(e -> callbacks.onError(e.getMessage()));
	}

	@Override
	public void createPost(@NonNull Post post, @NonNull PostOperationCallbacks callbacks) {
		firestore.collection(POSTS_COLLECTION)
				.add(post)
				.addOnSuccessListener(documentReference -> {
					String newId = documentReference.getId();
					post.setId(newId);
					documentReference.update("id", newId)
							.addOnSuccessListener(aVoid -> callbacks.onSuccess(post))
							.addOnFailureListener(e -> callbacks.onError("Failed to update post with its ID: " + e.getMessage()));
				})
				.addOnFailureListener(e -> callbacks.onError("Failed to create post: " + e.getMessage()));
	}
}