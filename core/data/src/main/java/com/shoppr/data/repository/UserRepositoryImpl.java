package com.shoppr.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.domain.datasource.FirestoreUserDataSource;
import com.shoppr.domain.repository.UserRepository;
import com.shoppr.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepositoryImpl implements UserRepository {
    private static final String TAG = "UserRepositoryImpl";
    private final FirestoreUserDataSource firestoreUserDataSource; // Using the interface

    @Inject
    public UserRepositoryImpl(FirestoreUserDataSource firestoreUserDataSource) { // Inject interface
        this.firestoreUserDataSource = firestoreUserDataSource;
    }

    @Override
    public void getOrCreateUserProfile(@NonNull String uid, @Nullable String displayName, @Nullable String email, @Nullable String photoUrl, @NonNull ProfileOperationCallbacks callbacks) {
        Log.d(TAG, "getOrCreateUserProfile called for UID: " + uid);
        firestoreUserDataSource.getUser(uid, new FirestoreUserDataSource.FirestoreOperationCallbacks() {
            @Override
            public void onSuccess(@NonNull User user) {
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
                        // Initialize new location fields to null or default
                        .lastLatitude(null)
                        .lastLongitude(null)
                        .lastLocationAddress(null)
                        .build();

                firestoreUserDataSource.createUser(newUser, new FirestoreUserDataSource.FirestoreOperationCallbacks() {
                    @Override
                    public void onSuccess(@NonNull User createdUser) {
                        Log.d(TAG, "Successfully created new user profile via DataSource for UID: " + createdUser.getId());
                        callbacks.onSuccess(createdUser);
                    }

                    @Override
                    public void onError(@NonNull String message) {
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
            public void onError(@NonNull String message) {
                Log.e(TAG, "Error getting user profile from DataSource for UID: " + uid + " - " + message);
                callbacks.onError(message);
            }
        });
    }

    @Override
    public void updateUserDefaultLocation(@NonNull String uid, double latitude, double longitude, @Nullable String addressName, @NonNull LocationUpdateCallbacks callbacks) {
        Log.d(TAG, "Attempting to update default location for UID: " + uid);
        // First, get the existing user data to ensure we don't overwrite other fields unintentionally
        // if we were to construct a new User object with only location data.
        // It's safer to fetch, modify, then update.
        firestoreUserDataSource.getUser(uid, new FirestoreUserDataSource.FirestoreOperationCallbacks() {
            @Override
            public void onSuccess(@NonNull User user) {
                Log.d(TAG, "User " + uid + " found. Updating location fields on the fetched User object.");
                user.setLastLatitude(latitude);
                user.setLastLongitude(longitude);
                user.setLastLocationAddress(addressName);

                // Now call the updateUser method in the DataSource with the modified User object
                firestoreUserDataSource.updateUser(user, new FirestoreUserDataSource.FirestoreOperationCallbacks() {
                    @Override
                    public void onSuccess(@NonNull User updatedUser) { // updateUser callback returns the User object
                        Log.d(TAG, "User " + uid + " location updated successfully in DataSource.");
                        callbacks.onSuccess(); // Domain callback for LocationUpdateCallbacks is void
                    }

                    @Override
                    public void onError(@NonNull String message) {
                        Log.e(TAG, "Error updating user " + uid + " location in DataSource: " + message);
                        callbacks.onError(message);
                    }

                    @Override
                    public void onNotFound() {
                        // This implies the user was deleted between the getUser and updateUser calls, which is rare.
                        Log.e(TAG, "User " + uid + " not found during updateUser call, though getUser succeeded. Inconsistent state?");
                        callbacks.onError("User not found during location update save operation.");
                    }
                });
            }

            @Override
            public void onNotFound() {
                Log.e(TAG, "User " + uid + " not found. Cannot update default location.");
                callbacks.onError("User profile not found, cannot update location.");
            }

            @Override
            public void onError(@NonNull String message) {
                Log.e(TAG, "Error fetching user " + uid + " before attempting location update: " + message);
                callbacks.onError("Could not retrieve user profile to update location: " + message);
            }
        });
    }
}