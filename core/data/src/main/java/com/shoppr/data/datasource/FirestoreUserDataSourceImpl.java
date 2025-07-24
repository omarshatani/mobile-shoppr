package com.shoppr.data.datasource;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.shoppr.domain.datasource.FirestoreUserDataSource;
import com.shoppr.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirestoreUserDataSourceImpl implements FirestoreUserDataSource {
	private static final String TAG = "FirestoreUserDSImpl";
	private final FirebaseFirestore firestore;
	private static final String USERS_COLLECTION = "users"; // Or from a constants file

	@Inject
	public FirestoreUserDataSourceImpl(FirebaseFirestore firestore) {
		this.firestore = firestore;
	}

	@Override
	public void getUser(@NonNull String uid, @NonNull FirestoreOperationCallbacks callbacks) {
		Log.d(TAG, "Getting user profile from Firestore for UID: " + uid);
		if (uid.isEmpty()) {
			callbacks.onError("User ID cannot be null or empty.");
			return;
		}
		DocumentReference userDocRef = firestore.collection(USERS_COLLECTION).document(uid);
		userDocRef.get()
				.addOnSuccessListener(documentSnapshot -> {
					if (documentSnapshot.exists()) {
						User user = documentSnapshot.toObject(User.class);
						if (user != null) {
							user.setId(documentSnapshot.getId()); // Ensure ID is set
							Log.d(TAG, "User profile found in Firestore. UID: " + user.getId() + ", Name: " + user.getName());
							callbacks.onSuccess(user);
						} else {
							Log.e(TAG, "Failed to map Firestore document to User object for UID: " + uid);
							callbacks.onError("Error mapping user data for UID: " + uid);
						}
					} else {
						Log.d(TAG, "User profile not found in Firestore for UID: " + uid);
						callbacks.onNotFound();
					}
				})
				.addOnFailureListener(e -> {
					Log.e(TAG, "Error fetching user profile from Firestore for UID: " + uid, e);
					callbacks.onError("Error fetching user profile: " + e.getMessage());
				});
	}

	@Override
	public void createUser(@NonNull User user, @NonNull FirestoreOperationCallbacks callbacks) {
		Log.d(TAG, "Creating user profile in Firestore for UID: " + user.getId());
		if (user.getId() == null || user.getId().isEmpty()) {
			Log.e(TAG, "Attempted to create user with null or empty ID.");
			callbacks.onError("User ID cannot be null or empty for creation.");
			return;
		}
		DocumentReference userDocRef = firestore.collection(USERS_COLLECTION).document(user.getId());
		userDocRef.set(user) // Set the entire user object for creation
				.addOnSuccessListener(aVoid -> {
					Log.d(TAG, "User profile successfully created in Firestore for UID: " + user.getId());
					callbacks.onSuccess(user);
				})
				.addOnFailureListener(e -> {
					Log.e(TAG, "Error creating user profile in Firestore for UID: " + user.getId(), e);
					callbacks.onError("Error creating user profile: " + e.getMessage());
				});
	}

	@Override
	public void updateUser(@NonNull User user, @NonNull FirestoreOperationCallbacks callbacks) {
		Log.d(TAG, "Updating user profile in Firestore for UID: " + user.getId());
		if (user.getId() == null || user.getId().isEmpty()) {
			callbacks.onError("User ID cannot be null or empty for update.");
			return;
		}
		firestore.collection(USERS_COLLECTION).document(user.getId())
				.set(user, SetOptions.merge()) // Use merge to update fields without overwriting everything else
				.addOnSuccessListener(aVoid -> {
					Log.d(TAG, "User profile successfully updated in Firestore: " + user.getId());
					// Firestore's set with merge doesn't return the object, so we return the one we passed in.
					// For a more robust approach, you might re-fetch the document or trust the input `user` object.
					callbacks.onSuccess(user);
				})
				.addOnFailureListener(e -> {
					Log.e(TAG, "Error updating user profile in Firestore for UID: " + user.getId(), e);
					callbacks.onError("Error updating user profile: " + e.getMessage());
				});
	}

	@Override
	public void updateUserFavorites(
			@NonNull String uid,
			@NonNull String postId,
			boolean shouldAdd,
			@NonNull FavoriteUpdateCallbacks callbacks
	) {
		firestore.collection("users").document(uid)
				.update("favoritePosts", shouldAdd ? FieldValue.arrayUnion(postId) : FieldValue.arrayRemove(postId))
				.addOnSuccessListener(aVoid -> {
					firestore.collection("users").document(uid).get()
							.addOnSuccessListener(documentSnapshot -> {
								if (documentSnapshot.exists()) {
									User updatedUser = documentSnapshot.toObject(User.class);
									if (updatedUser != null) {
										callbacks.onSuccess(updatedUser.getFavoritePosts());
									} else {
										callbacks.onError("Failed to parse updated user data.");
									}
								} else {
									callbacks.onError("User document not found after update.");
								}
							})
							.addOnFailureListener(e -> callbacks.onError("Failed to fetch updated favorites: " + e.getMessage()));
				})
				.addOnFailureListener(e -> callbacks.onError("Failed to update favorites: " + e.getMessage()));
	}
}
