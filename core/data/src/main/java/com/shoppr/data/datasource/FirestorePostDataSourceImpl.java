package com.shoppr.data.datasource;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.shoppr.model.Post;

import javax.inject.Inject;

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
        // Generate an ID if the post doesn't have one (for new posts)
        DocumentReference postRef;
        if (post.getId() == null || post.getId().isEmpty()) { // Assuming Post has getId/setId
            postRef = firestore.collection(POSTS_COLLECTION).document();
            post.setId(postRef.getId()); // Set the generated ID back on the domain object
            Log.d(TAG, "Saving new post with generated ID: " + post.getId());
        } else {
            postRef = firestore.collection(POSTS_COLLECTION).document(post.getId());
            Log.d(TAG, "Updating existing post with ID: " + post.getId());
        }

        // Add server timestamp for creation/update if not already set by UseCase/Repository
        // if (post.getCreatedAt() == null) post.setCreatedAt(FieldValue.serverTimestamp());
        // post.setUpdatedAt(FieldValue.serverTimestamp());

        postRef.set(post, SetOptions.merge()) // Use merge to allow updates without overwriting all fields
            .addOnSuccessListener(aVoid -> {
                Log.i(TAG, "Post successfully saved to Firestore: " + post.getId());
                callbacks.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving post to Firestore: " + post.getId(), e);
                callbacks.onError("Failed to save post: " + e.getMessage());
            });
    }

	@Override
	public void getPostById(@NonNull String postId, @NonNull FirestoreGetPostCallbacks callbacks) {
		// TODO: Implement
	}

	@Override
	public void updatePost(@NonNull Post post) {
		// TODO: Implement
	}

	@Override
	public void deletePost(@NonNull String postId) {
		// TODO: Implement
	}
}