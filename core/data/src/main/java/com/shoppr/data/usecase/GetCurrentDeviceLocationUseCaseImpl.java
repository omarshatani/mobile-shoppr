package com.shoppr.data.usecase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.shoppr.domain.GetCurrentDeviceLocationUseCase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GetCurrentDeviceLocationUseCaseImpl implements GetCurrentDeviceLocationUseCase {
    private static final String TAG = "GetDeviceLocationUCImpl";
    private final Application application; // For context to get FusedLocationProviderClient & Geocoder
    private final FusedLocationProviderClient fusedLocationClient;

    @Inject
    public GetCurrentDeviceLocationUseCaseImpl(Application application) {
        this.application = application;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void execute(@NonNull GetDeviceLocationCallbacks callbacks) {
        if (ContextCompat.checkSelfPermission(application, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(application, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted when trying to execute GetCurrentDeviceLocationUseCase.");
            callbacks.onDeviceLocationError("Location permission not granted.");
            return;
        }

        Log.d(TAG, "Attempting to fetch current device location.");
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Log.d(TAG, "Device location fetched successfully: Lat " + location.getLatitude() + ", Lon " + location.getLongitude());
                            String addressString = getAddressFromLocation(location);
                            callbacks.onDeviceLocationSuccess(new DeviceLocation(location.getLatitude(), location.getLongitude(), addressString));
                        } else {
                            Log.w(TAG, "FusedLocationProviderClient returned null location.");
                            callbacks.onDeviceLocationError("Unable to retrieve current location (device returned null).");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting current location from FusedLocationProviderClient", e);
                        callbacks.onDeviceLocationError("Error getting current location: " + e.getMessage());
                    });
        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException while trying to get current location. Permissions might have been revoked.", se);
            callbacks.onDeviceLocationError("Location permission issue: " + se.getMessage());
        }
    }

    @Nullable
    private String getAddressFromLocation(Location location) {
        if (!Geocoder.isPresent()) {
            Log.w(TAG, "Geocoder not present, cannot reverse geocode location.");
            return null;
        }
        Geocoder geocoder = new Geocoder(application, Locale.getDefault());
        try {
            // In Android Q and above, getFromLocation might return an empty list or throw IOException
            // if the geocoder backend service is not available.
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String addressString = getAddressFrom(addresses);
                Log.d(TAG, "Reverse geocoded address: " + addressString);
                return addressString.isEmpty() ? null : addressString;
            } else {
                Log.w(TAG, "No address found by Geocoder for the location.");
            }
        } catch (IOException e) {
            // This can happen if the network is down or the geocoding service is unavailable.
            Log.e(TAG, "Geocoder IOException while trying to get address from location", e);
        }
        return null;
    }

    @NonNull
    private static String getAddressFrom(List<Address> addresses) {
        Address address = addresses.get(0);
        StringBuilder sb = new StringBuilder();
        // Construct a readable address string
        if (address.getMaxAddressLineIndex() >= 0) {
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                sb.append(address.getAddressLine(i));
                if (i < address.getMaxAddressLineIndex()) sb.append(", ");
            }
        } else {
            // Fallback if getAddressLine is not available
            if (address.getFeatureName() != null) sb.append(address.getFeatureName()).append(", ");
            if (address.getThoroughfare() != null)
                sb.append(address.getThoroughfare()).append(", ");
            if (address.getLocality() != null) sb.append(address.getLocality()).append(", ");
            if (address.getAdminArea() != null) sb.append(address.getAdminArea()).append(", ");
            if (address.getCountryName() != null) sb.append(address.getCountryName());
        }
        return sb.toString().trim();
    }
}