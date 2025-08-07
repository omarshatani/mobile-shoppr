package com.shoppr.domain.datasource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.model.User;

public interface FirestoreUserDataSource {

    /**
     * Callbacks for operations that return a full User object.
     */
    interface UserCallbacks {
        void onSuccess(@NonNull User user);
        void onError(@NonNull String message);
    }

    /**
     * Callbacks for simple success/error operations.
     */
    interface OperationCallbacks {
        void onSuccess();

        void onError(@NonNull String message);
    }

    /**
     * Fetches a user profile from Firestore. If it doesn't exist, it creates one.
     */
    void getOrCreateUserProfile(
        @NonNull String uid,
        @Nullable String displayName,
        @Nullable String email,
        @Nullable String photoUrl,
        @NonNull UserCallbacks callbacks
    );

    /**
     * Updates the location fields for a specific user.
     */
    void updateUserLocation(
        @NonNull String uid,
        double latitude,
        double longitude,
        @Nullable String addressName,
        @NonNull OperationCallbacks callbacks
    );

    /**
     * Atomically adds or removes a postId from the user's favoritePosts list.
     */
    void updateUserFavorites(
        @NonNull String uid,
        @NonNull String postId,
        boolean shouldAdd,
        @NonNull OperationCallbacks callbacks
    );
}