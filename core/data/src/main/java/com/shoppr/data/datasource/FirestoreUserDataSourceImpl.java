package com.shoppr.data.datasource;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.shoppr.domain.datasource.FirestoreUserDataSource;
import com.shoppr.model.User;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirestoreUserDataSourceImpl implements FirestoreUserDataSource {
	private static final String TAG = "FirestoreUserDSImpl";
	private final FirebaseFirestore firestore;
	private static final String USERS_COLLECTION = "users";

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
							user.setId(documentSnapshot.getId());
							Log.d(TAG, "User profile found in Firestore. UID: " + user.getId());
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
		userDocRef.set(user)
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
				.set(user, SetOptions.merge())
				.addOnSuccessListener(aVoid -> {
					Log.d(TAG, "User profile successfully updated in Firestore: " + user.getId());
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
		final DocumentReference userDocRef = firestore.collection(USERS_COLLECTION).document(uid);

		firestore.runTransaction((Transaction.Function<Void>) transaction -> {
			DocumentSnapshot snapshot = transaction.get(userDocRef);
			User user = snapshot.toObject(User.class);

			if (user == null) {
				// This case should be rare if the user is logged in, but it's good practice.
				throw new IllegalStateException("User document not found for UID: " + uid);
			}

			List<String> favorites = user.getFavoritePosts();
			if (favorites == null) {
				favorites = new ArrayList<>();
			}

			// Perform the add or remove operation on the list in memory
			if (shouldAdd) {
				if (!favorites.contains(postId)) {
					favorites.add(postId);
				}
			} else {
				favorites.remove(postId);
			}

			// Update the field in the transaction
			transaction.update(userDocRef, "favoritePosts", favorites);

			// The transaction will return null on success. We handle the callback outside.
			return null;
		}).addOnSuccessListener(aVoid -> {
			Log.d(TAG, "Transaction success: Favorites updated for user " + uid);
			// We now need to re-fetch the user to get the guaranteed latest state
			getUser(uid, new FirestoreOperationCallbacks() {
				@Override
				public void onSuccess(@NonNull User updatedUser) {
					callbacks.onSuccess(updatedUser.getFavoritePosts());
				}

				@Override
				public void onError(@NonNull String message) {
					callbacks.onError(message);
				}

				@Override
				public void onNotFound() {
					callbacks.onError("User not found after favorite update.");
				}
			});
		}).addOnFailureListener(e -> {
			Log.e(TAG, "Transaction failure: " + e.getMessage());
			callbacks.onError("Failed to update favorites: " + e.getMessage());
		});
	}
}