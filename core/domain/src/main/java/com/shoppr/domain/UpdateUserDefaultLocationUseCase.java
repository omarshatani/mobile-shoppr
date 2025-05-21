package com.shoppr.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface UpdateUserDefaultLocationUseCase {
    interface UpdateLocationCallbacks {
        void onLocationUpdateSuccess();

        void onLocationUpdateError(@NonNull String message);
    }

    void execute(
            @NonNull String uid,
            double latitude,
            double longitude,
            @Nullable String addressName,
            @NonNull UpdateLocationCallbacks callbacks
    );
}