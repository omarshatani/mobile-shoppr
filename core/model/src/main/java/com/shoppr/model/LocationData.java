package com.shoppr.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LocationData {
    public final Double latitude;
    public final Double longitude;
    @Nullable
    public final String addressString;

    public LocationData(@Nullable Double latitude, @Nullable Double longitude, @Nullable String addressString) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressString = addressString;
    }

    @NonNull
    @Override
    public String toString() {
        return "LocationData{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", addressString='" + addressString + '\'' +
                '}';
    }
}
