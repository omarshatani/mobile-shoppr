package com.shoppr.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.model.User;

public interface UserRepository {
    interface ProfileOperationCallbacks {
        void onSuccess(@NonNull User user);

        void onError(@NonNull String message);
    }

    void getOrCreateUserProfile(
            @NonNull String uid,
            @Nullable String displayName,
            @Nullable String email,
            @Nullable String photoUrl,
            @NonNull ProfileOperationCallbacks callbacks
    );

    interface LocationUpdateCallbacks {
        void onSuccess();

        void onError(@NonNull String message);
    }

    void updateUserDefaultLocation(
            @NonNull String uid,
            double latitude,
            double longitude,
            @Nullable String addressName,
            @NonNull LocationUpdateCallbacks callbacks
    );
}