package com.shoppr.data.datasource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.shoppr.domain.datasource.FirestoreUserDataSource;
import com.shoppr.model.User;

import java.util.HashMap;
import java.util.Map;

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
	public void getOrCreateUserProfile(
			@NonNull String uid,
			@Nullable String displayName,
			@Nullable String email,
			@Nullable String photoUrl,
			@NonNull UserCallbacks callbacks
	) {
		DocumentReference userDocRef = firestore.collection(USERS_COLLECTION).document(uid);
		userDocRef.get().addOnSuccessListener(documentSnapshot -> {
			if (documentSnapshot.exists()) {
				User user = documentSnapshot.toObject(User.class);
				if (user != null) {
					user.setId(documentSnapshot.getId());
					callbacks.onSuccess(user);
				} else {
					callbacks.onError("Error mapping user data.");
				}
			} else {
				// User doesn't exist, create a new one
				User newUser = new User.Builder()
						.id(uid)
						.name(displayName)
						.email(email)
						// photoUrl would be set here if it was part of your User model
						.build();
				userDocRef.set(newUser).addOnSuccessListener(aVoid -> {
					callbacks.onSuccess(newUser);
				}).addOnFailureListener(e -> {
					callbacks.onError("Failed to create user profile: " + e.getMessage());
				});
			}
		}).addOnFailureListener(e -> {
			callbacks.onError("Failed to fetch user profile: " + e.getMessage());
		});
	}

	@Override
	public void updateUserLocation(
			@NonNull String uid,
			double latitude,
			double longitude,
			@Nullable String addressName,
			@NonNull OperationCallbacks callbacks
	) {
		Map<String, Object> locationData = new HashMap<>();
		locationData.put("latitude", latitude);
		locationData.put("longitude", longitude);
		locationData.put("locationAddress", addressName);

		firestore.collection(USERS_COLLECTION).document(uid)
				.set(locationData, SetOptions.merge())
				.addOnSuccessListener(aVoid -> callbacks.onSuccess())
				.addOnFailureListener(e -> callbacks.onError("Failed to update location: " + e.getMessage()));
	}

	@Override
	public void updateUserFavorites(
			@NonNull String uid,
			@NonNull String postId,
			boolean shouldAdd,
			@NonNull OperationCallbacks callbacks
	) {
		firestore.collection(USERS_COLLECTION).document(uid)
				.update("favoritePosts", shouldAdd ? FieldValue.arrayUnion(postId) : FieldValue.arrayRemove(postId))
				.addOnSuccessListener(aVoid -> callbacks.onSuccess())
				.addOnFailureListener(e -> callbacks.onError("Failed to update favorites: " + e.getMessage()));
	}

	@Override
	public void getUserById(String userId, @NonNull FirestoreUserDataSource.GetUserByIdCallbacks callbacks) {
		firestore.collection("users").document(userId).get()
				.addOnSuccessListener(documentSnapshot -> {
					if (documentSnapshot != null && documentSnapshot.exists()) {
						User user = documentSnapshot.toObject(User.class);
						callbacks.onSuccess(user);
					} else {
						callbacks.onSuccess(null);
					}
				})
				.addOnFailureListener(e -> callbacks.onError("Error fetching user: " + e.getMessage()));
	}
}