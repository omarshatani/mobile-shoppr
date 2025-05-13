package com.shoppr.data.repository;

import android.util.Log;

import androidx.annotation.Nullable;

import com.shoppr.data.datasource.FirestoreUserDataSource;
import com.shoppr.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

 @Singleton
 public class UserRepositoryImpl implements UserRepository {
    private static final String TAG_USER_REPO_IMPL = "UserRepoImpl";
    private final FirestoreUserDataSource FirestoreUserDataSource;

    @Inject
    public UserRepositoryImpl(FirestoreUserDataSource FirestoreUserDataSource) {
        this.FirestoreUserDataSource = FirestoreUserDataSource;
    }

    @Override
    public void getOrCreateUserProfile(String uid, @Nullable String displayName, @Nullable String email, @Nullable String photoUrl,
                                       ProfileOperationCallbacks callbacks) {
        Log.d(TAG_USER_REPO_IMPL, "getOrCreateUserProfile for UID: " + uid);
        FirestoreUserDataSource.getUser(uid, new FirestoreUserDataSource.FirestoreOperationCallbacks() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG_USER_REPO_IMPL, "User found in DataSource: " + user.getId());
                callbacks.onSuccess(user);
            }

            @Override
            public void onNotFound() {
                Log.d(TAG_USER_REPO_IMPL, "User not found in DataSource, creating: " + uid);
                User newUser = new User.Builder()
                    .id(uid)
                    .name(displayName)
                    .email(email)
                    // photoUrl not directly part of User model for creation from auth info
                    // other fields like phoneNumber, address would be default or empty
                    .build();
                FirestoreUserDataSource.createUser(newUser, new FirestoreUserDataSource.FirestoreOperationCallbacks() {
                    @Override public void onSuccess(User createdUser) { callbacks.onSuccess(createdUser); }
                    @Override public void onError(String message) { callbacks.onError(message); }
                    @Override public void onNotFound() { /* Should not happen during create */ }
                });
            }

            @Override
            public void onError(String message) {
                Log.e(TAG_USER_REPO_IMPL, "Error getting user from DataSource: " + message);
                callbacks.onError(message);
            }
        });
    }
 }