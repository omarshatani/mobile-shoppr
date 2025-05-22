package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface GetCurrentDeviceLocationUseCase {
    class DeviceLocation {
        public final double latitude;
        public final double longitude;
        @Nullable
        public final String address;

        public DeviceLocation(double latitude, double longitude, @Nullable String address) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
        }
    }

    interface GetDeviceLocationCallbacks {
        void onDeviceLocationSuccess(@NonNull DeviceLocation location);

        void onDeviceLocationError(@NonNull String message);
    }

    void execute(@NonNull GetDeviceLocationCallbacks callbacks);
}