package com.shoppr.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.data.datasource.FirestoreUserDataSource;
import com.shoppr.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepositoryImpl implements UserRepository {
	private static final String TAG = "UserRepositoryImpl";
	private final FirestoreUserDataSource firestoreUserDataSource; // Using the interface

	@Inject
	public UserRepositoryImpl(FirestoreUserDataSource firestoreUserDataSource) {
		this.firestoreUserDataSource = firestoreUserDataSource;
	}

	@Override
	public void getOrCreateUserProfile(String uid, @Nullable String displayName, @Nullable String email, @Nullable String photoUrl,
																		 @NonNull ProfileOperationCallbacks callbacks) {
		Log.d(TAG, "getOrCreateUserProfile called for UID: " + uid);
		firestoreUserDataSource.getUser(uid, new FirestoreUserDataSource.FirestoreOperationCallbacks() {
			@Override
			public void onSuccess(User user) {
				Log.d(TAG, "User profile found in Firestore via DataSource for UID: " + user.getId());
				callbacks.onSuccess(user);
			}

			@Override
			public void onNotFound() {
				Log.d(TAG, "User profile not found in Firestore via DataSource for UID: " + uid + ". Attempting to create.");
				User newUser = new User.Builder()
						.id(uid)
						.name(displayName)
						.email(email)
						// photoUrl is not directly part of your User model for creation from auth info.
						// If you store it in Firestore, the FirestoreUserDataSource.createUser would handle it
						// if it's passed to the User object.
						.build();

				firestoreUserDataSource.createUser(newUser, new FirestoreUserDataSource.FirestoreOperationCallbacks() {
					@Override
					public void onSuccess(User createdUser) {
						Log.d(TAG, "Successfully created new user profile via DataSource for UID: " + createdUser.getId());
						callbacks.onSuccess(createdUser);
					}

					@Override
					public void onError(String message) {
						Log.e(TAG, "Error creating new user profile via DataSource for UID: " + uid + " - " + message);
						callbacks.onError(message);
					}

					@Override
					public void onNotFound() {
						// This case should ideally not be reached during a createUser call's callback.
						Log.e(TAG, "onNotFound called unexpectedly during createUser callback for UID: " + uid);
						callbacks.onError("Unexpected error during profile creation (onNotFound).");
					}
				});
			}

			@Override
			public void onError(String message) {
				Log.e(TAG, "Error getting user profile from DataSource for UID: " + uid + " - " + message);
				callbacks.onError(message);
			}
		});
	}
}