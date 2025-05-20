package com.shoppr.model;

import androidx.annotation.Nullable;

public class LocationData {
    public final double latitude;
    public final double longitude;
    @Nullable
    public final String addressString;

    public LocationData(double latitude, double longitude, @Nullable String addressString) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressString = addressString;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Nullable
    public String getAddressString() {
        return addressString;
    }
}
