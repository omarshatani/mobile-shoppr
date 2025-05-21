package com.shoppr.map;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.model.LatLng;
import com.shoppr.domain.usecase.GetCurrentDeviceLocationUseCase;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.UpdateUserDefaultLocationUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.User;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MapViewModel extends AndroidViewModel {
    private static final String TAG = "MapViewModel";

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final GetCurrentDeviceLocationUseCase getCurrentDeviceLocationUseCase;
    private final UpdateUserDefaultLocationUseCase updateUserDefaultLocationUseCase;

    public final LiveData<User> currentUserProfileLiveData; // User with last known location

    private final MutableLiveData<Boolean> _locationPermissionGranted = new MutableLiveData<>(false);
    public LiveData<Boolean> locationPermissionGranted = _locationPermissionGranted;

    private final MutableLiveData<Integer> _fabIconResId = new MutableLiveData<>(com.shoppr.core.ui.R.drawable.ic_gps_fixed); // Default icon
    public LiveData<Integer> fabIconResId = _fabIconResId;

    private final MutableLiveData<Event<LatLng>> _moveToLocationEvent = new MutableLiveData<>();
    public LiveData<Event<LatLng>> moveToLocationEvent = _moveToLocationEvent;

    private final MutableLiveData<Event<Boolean>> _requestPermissionEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> requestPermissionEvent = _requestPermissionEvent;

    private final MutableLiveData<Event<String>> _toastMessageEvent = new MutableLiveData<>();
    public LiveData<Event<String>> toastMessageEvent = _toastMessageEvent;

    private boolean isMapManuallyMoved = false;
    private boolean initialLocationSet = false;

    private final Observer<User> userObserver;

    @Inject
    public MapViewModel(@NonNull Application application,
                        GetCurrentUserUseCase getCurrentUserUseCase,
                        GetCurrentDeviceLocationUseCase getCurrentDeviceLocationUseCase,
                        UpdateUserDefaultLocationUseCase updateUserDefaultLocationUseCase) {
        super(application);
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.getCurrentDeviceLocationUseCase = getCurrentDeviceLocationUseCase;
        this.updateUserDefaultLocationUseCase = updateUserDefaultLocationUseCase;

        this.currentUserProfileLiveData = this.getCurrentUserUseCase.getFullUserProfile();

        userObserver = user -> {
            if (user != null && !initialLocationSet) {
                if (user.getLastLatitude() != null && user.getLastLongitude() != null) {
                    Log.d(TAG, "User profile loaded. Centering map on last known location: " +
                            user.getLastLatitude() + ", " + user.getLastLongitude());
                    _moveToLocationEvent.postValue(new Event<>(new LatLng(user.getLastLatitude(), user.getLastLongitude())));
                    initialLocationSet = true;
                } else {
                    Log.d(TAG, "User profile loaded, but no last known location. Will attempt to fetch current location if permission granted.");
                    // If permission is already granted, try fetching current location
                    if (Boolean.TRUE.equals(_locationPermissionGranted.getValue())) {
                        fetchAndSaveDeviceLocation();
                    }
                }
            } else if (user == null) {
                Log.d(TAG, "User logged out or not available.");
                initialLocationSet = false; // Reset for next login
            }
        };
    }

    public void onMapFragmentStarted() {
        Log.d(TAG, "MapFragment started. Starting auth and user profile observation.");
        getCurrentUserUseCase.startObserving(); // Start observing the user's full profile
        currentUserProfileLiveData.observeForever(userObserver); // Observe for initial location
    }

    public void onMapFragmentStopped() {
        Log.d(TAG, "MapFragment stopped. Stopping auth and user profile observation.");
        currentUserProfileLiveData.removeObserver(userObserver); // Clean up observer
        getCurrentUserUseCase.stopObserving();
    }

    public void onLocationSearching() {
        _fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_location_searching);
    }

    public void onLocationPermissionResult(boolean isGranted) {
        Log.d(TAG, "Location permission result: " + isGranted);
        _locationPermissionGranted.setValue(isGranted);
        if (isGranted) {
            _fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_gps_fixed); // Standard icon
            fetchAndSaveDeviceLocation();
        } else {
            _fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_location_disabled); // Disabled icon
        }
    }

    public void onMyLocationButtonClicked() {
        Log.d(TAG, "My Location FAB clicked.");
        if (Boolean.TRUE.equals(_locationPermissionGranted.getValue())) {
            Log.d(TAG, "Permission granted, fetching device location.");
            fetchAndSaveDeviceLocation();
            isMapManuallyMoved = false; // Reset manual move flag, as user wants to center
        } else {
            Log.d(TAG, "Permission not granted, requesting permission.");
            _requestPermissionEvent.setValue(new Event<>(true)); // Signal Fragment to request
        }
    }

    public void onMapManualMoveStarted() {
        Log.d(TAG, "Map manually moved by user.");
        isMapManuallyMoved = true;
        // Optionally change FAB icon to indicate "re-center" mode if desired
        onLocationSearching();
    }

    private void fetchAndSaveDeviceLocation() {
        User currentUser = currentUserProfileLiveData.getValue();
        if (currentUser == null || currentUser.getId() == null) {
            Log.w(TAG, "Cannot fetch and save device location: current user or UID is null.");
            _toastMessageEvent.postValue(new Event<>("Login required to save location."));
            return;
        }
        final String currentUserId = currentUser.getId();

        Log.d(TAG, "Attempting to fetch device location for user: " + currentUserId);
        // Potentially show a loading indicator on FAB or map
        // _fabIconResId.setValue(R.drawable.ic_location_searching_24); // Example searching icon

        getCurrentDeviceLocationUseCase.execute(new GetCurrentDeviceLocationUseCase.GetDeviceLocationCallbacks() {
            @Override
            public void onDeviceLocationSuccess(@NonNull GetCurrentDeviceLocationUseCase.DeviceLocation deviceLocation) {
                Log.i(TAG, "Device location fetched: " + deviceLocation.latitude + "," + deviceLocation.longitude);
                if (!isMapManuallyMoved || !initialLocationSet) { // Only move camera if not manually panned or if it's the first set
                    _moveToLocationEvent.postValue(new Event<>(new LatLng(deviceLocation.latitude, deviceLocation.longitude)));
                    initialLocationSet = true;
                }
                // _fabIconResId.setValue(R.drawable.ic_my_location_24); // Reset to default

                // Update user's profile with this new location in Firestore
                updateUserDefaultLocationUseCase.execute(
                        currentUserId,
                        deviceLocation.latitude,
                        deviceLocation.longitude,
                        deviceLocation.address, // The reverse-geocoded address from the use case
                        new UpdateUserDefaultLocationUseCase.UpdateLocationCallbacks() {
                            @Override
                            public void onLocationUpdateSuccess() {
                                Log.i(TAG, "User default location updated in Firestore for user: " + currentUserId);
                                // Optionally show a subtle success message
                                // _toastMessageEvent.postValue(new Event<>("Your location has been updated."));
                            }

                            @Override
                            public void onLocationUpdateError(@NonNull String message) {
                                Log.e(TAG, "Failed to update user default location in Firestore: " + message);
                                // _toastMessageEvent.postValue(new Event<>("Could not save your current location."));
                            }
                        }
                );
            }

            @Override
            public void onDeviceLocationError(@NonNull String message) {
                Log.e(TAG, "Failed to fetch device location: " + message);
                // _fabIconResId.setValue(R.drawable.ic_my_location_24); // Reset to default or error icon
                if (!message.toLowerCase().contains("permission")) { // Don't toast for permission denial again
                    _toastMessageEvent.postValue(new Event<>("Could not get current location: " + message));
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "MapViewModel onCleared. Removing observer.");
        currentUserProfileLiveData.removeObserver(userObserver);
    }
}
