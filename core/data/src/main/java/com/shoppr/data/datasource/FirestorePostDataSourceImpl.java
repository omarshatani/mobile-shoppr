package com.shoppr.data.datasource;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.shoppr.domain.datasource.FirestorePostDataSource;
import com.shoppr.model.ListingState;
import com.shoppr.model.Post;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirestorePostDataSourceImpl implements FirestorePostDataSource {
	private static final String TAG = "FirestorePostDSImpl";
	private static final String POSTS_COLLECTION = "posts";
	private final FirebaseFirestore firestore;

	@Inject
	public FirestorePostDataSourceImpl(FirebaseFirestore firestore) {
		this.firestore = firestore;
	}

	@Override
	public void savePost(@NonNull Post post, @NonNull final FirestorePostCallbacks callbacks) {
		DocumentReference postRef;
		// Add server timestamp for creation if not already set (conceptual)
		// if (post.getCreatedAt() == null) { post.setCreatedAt(FieldValue.serverTimestamp()); }
		// post.setUpdatedAt(FieldValue.serverTimestamp());

		if (post.getId() == null || post.getId().isEmpty()) {
			// New post, generate ID
			postRef = firestore.collection(POSTS_COLLECTION).document();
			post.setId(postRef.getId()); // Set the generated ID back on the domain object
			Log.d(TAG, "Saving new post with generated ID: " + post.getId());
			postRef.set(post)
					.addOnSuccessListener(aVoid -> {
						Log.i(TAG, "New post successfully saved: " + post.getId());
						callbacks.onSuccess();
					})
					.addOnFailureListener(e -> {
						Log.e(TAG, "Error saving new post: " + post.getId(), e);
						callbacks.onError("Failed to save new post: " + e.getMessage());
					});
		} else {
			// Existing post, update
			postRef = firestore.collection(POSTS_COLLECTION).document(post.getId());
			Log.d(TAG, "Updating existing post with ID: " + post.getId());
			postRef.set(post, SetOptions.merge()) // Use merge to update fields
					.addOnSuccessListener(aVoid -> {
						Log.i(TAG, "Post successfully updated: " + post.getId());
						callbacks.onSuccess();
					})
					.addOnFailureListener(e -> {
						Log.e(TAG, "Error updating post: " + post.getId(), e);
						callbacks.onError("Failed to update post: " + e.getMessage());
					});
		}
	}

	@Override
	public LiveData<List<Post>> getPostsForMap(@Nullable String currentUserIdToExclude) {
		MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();
		Query query = firestore.collection(POSTS_COLLECTION)
				.whereEqualTo("state", ListingState.NEW.name()); // Use enum.name() if storing as string
		// Or .whereEqualTo("active", true) if using a boolean field

		// For real-time updates, use addSnapshotListener:
		query.addSnapshotListener((snapshot, e) -> {
			if (e != null) {
				Log.e(TAG, "Error listening for posts for map: ", e);
				postsLiveData.setValue(new ArrayList<>()); // Empty list on error or null
				return;
			}

			List<Post> posts = new ArrayList<>();
			if (snapshot == null) {
				postsLiveData.setValue(posts);
				Log.d(TAG, "Snapshot is null, no posts for map (real-time).");
				return;
			}

			for (DocumentSnapshot doc : snapshot.getDocuments()) {
				Post post = doc.toObject(Post.class);
				if (post == null) continue;

				post.setId(doc.getId()); // Ensure ID is set

				boolean shouldExclude = currentUserIdToExclude != null && post.getLister() != null &&
						currentUserIdToExclude.equals(post.getLister().getId());
				if (shouldExclude) continue;

				if (post.getLatitude() != null && post.getLongitude() != null) { // Only include posts with location
					posts.add(post);
				} else {
					Log.w(TAG, "Post " + post.getId() + " missing location, not adding to map.");
				}
			}
			postsLiveData.setValue(posts);
			Log.d(TAG, "Fetched " + posts.size() + " posts for map (real-time).");
		});
		return postsLiveData;
	}

	@Override
	public void getPostById(@NonNull String postId, @NonNull final FirestoreGetPostByIdCallbacks callbacks) {
		if (postId.isEmpty()) {
			callbacks.onError("Post ID cannot be null or empty.");
			return;
		}
		firestore.collection(POSTS_COLLECTION).document(postId).get()
				.addOnSuccessListener(documentSnapshot -> {
					if (documentSnapshot.exists()) {
						Post post = documentSnapshot.toObject(Post.class);
						if (post != null) {
							post.setId(documentSnapshot.getId()); // Ensure ID is set
							callbacks.onSuccess(post);
						} else {
							Log.e(TAG, "Failed to map Firestore document to Post object for ID: " + postId);
							callbacks.onError("Failed to map post data.");
						}
					} else {
						Log.d(TAG, "Post not found in Firestore for ID: " + postId);
						callbacks.onNotFound();
					}
				})
				.addOnFailureListener(e -> {
					Log.e(TAG, "Error fetching post by ID: " + postId, e);
					callbacks.onError(e.getMessage());
				});
	}

	@Override
	public void updatePost(@NonNull Post post) {
		// TODO Implement
	}

	@Override
	public void deletePost(@NonNull String postId) {
		// TODO Implement
	}
}