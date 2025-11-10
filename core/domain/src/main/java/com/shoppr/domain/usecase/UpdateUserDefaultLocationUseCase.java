package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface UpdateUserDefaultLocationUseCase {

    interface UpdateLocationCallbacks {
        void onLocationUpdateSuccess();
        void onLocationUpdateError(@NonNull String message);
    }

    void execute(
				double latitude,
				double longitude,
				@Nullable String addressName,
				@NonNull UpdateLocationCallbacks callbacks
    );
}