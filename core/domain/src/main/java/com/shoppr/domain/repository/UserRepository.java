package com.shoppr.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.shoppr.model.User;

public interface UserRepository {

    LiveData<User> getFullUserProfile();

    interface OperationCallbacks {
        void onSuccess();

        void onError(@NonNull String message);
    }

    interface ProfileOperationCallbacks {
        void onSuccess(@NonNull User user);
        void onError(@NonNull String message);
    }

    void startObservingUserProfile();

    void stopObservingUserProfile();

    void getOrCreateUserProfile(
        @NonNull String uid,
        @Nullable String displayName,
        @Nullable String email,
        @Nullable String photoUrl,
        @NonNull ProfileOperationCallbacks callbacks
    );

    void updateUserDefaultLocation(
        double latitude,
        double longitude,
        @Nullable String addressName,
        @NonNull OperationCallbacks callbacks
    );

    void toggleFavoriteStatus(
        @NonNull String postId,
        @NonNull OperationCallbacks callbacks
    );
}