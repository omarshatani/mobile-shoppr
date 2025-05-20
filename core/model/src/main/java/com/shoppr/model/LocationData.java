package com.shoppr.model;

import androidx.annotation.Nullable;

public class LocationData {
    public final Double latitude;
    public final Double longitude;
    @Nullable
    public final String addressString;

    public LocationData(Double latitude, Double longitude, @Nullable String addressString) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressString = addressString;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @Nullable
    public String getAddressString() {
        return addressString;
    }
}
