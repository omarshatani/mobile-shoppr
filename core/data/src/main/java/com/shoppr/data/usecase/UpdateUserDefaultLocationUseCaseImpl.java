package com.shoppr.data.usecase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.domain.repository.UserRepository;
import com.shoppr.domain.usecase.UpdateUserDefaultLocationUseCase;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UpdateUserDefaultLocationUseCaseImpl implements UpdateUserDefaultLocationUseCase {
    private static final String TAG = "UpdateUserLocUCImpl";
    private final UserRepository userRepository;

    @Inject
    public UpdateUserDefaultLocationUseCaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void execute(@NonNull String uid, double latitude, double longitude, @Nullable String addressName, @NonNull UpdateLocationCallbacks callbacks) {
        Log.d(TAG, "Executing for UID: " + uid + " Lat: " + latitude + " Lon: " + longitude);
        userRepository.updateUserDefaultLocation(uid, latitude, longitude, addressName, new UserRepository.LocationUpdateCallbacks() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Location update success for UID: " + uid);
                callbacks.onLocationUpdateSuccess();
            }

            @Override
            public void onError(@NonNull String message) {
                Log.e(TAG, "Location update error for UID: " + uid + " - " + message);
                callbacks.onLocationUpdateError(message);
            }
        });
    }
}