package com.shoppr.map;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.shoppr.ui.utils.Event;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MapViewModel extends ViewModel {
	private final static String TAG = "MapViewModel";
	private final FusedLocationProviderClient fusedLocationClient;

	// --- State LiveData ---
	// Permission Granted State
	private final MutableLiveData<Boolean> _locationPermissionGranted = new MutableLiveData<>(false);
	public LiveData<Boolean> locationPermissionGranted = _locationPermissionGranted;

	// Is the camera currently supposed to be tracking the user's location?
	private final MutableLiveData<Boolean> _isCameraTrackingUser = new MutableLiveData<>(false);
	public LiveData<Boolean> isCameraTrackingUser = _isCameraTrackingUser;

	// FAB Icon State (Resource ID)
	private final MutableLiveData<Integer> _fabIconResId = new MutableLiveData<>(com.shoppr.core.ui.R.drawable.ic_location_disabled);
	public LiveData<Integer> fabIconResId = _fabIconResId;

	// --- Event LiveData ---
	// Event to tell the Fragment to move the camera
	private final MutableLiveData<Event<LatLng>> _moveToLocationEvent = new MutableLiveData<>();
	public LiveData<Event<LatLng>> moveToLocationEvent = _moveToLocationEvent;

	// Event to tell the Fragment to request permission
	private final MutableLiveData<Event<Boolean>> _requestPermissionEvent = new MutableLiveData<>();
	public LiveData<Event<Boolean>> requestPermissionEvent = _requestPermissionEvent;

	@Inject
	public MapViewModel(FusedLocationProviderClient fusedLocationClient) {
		this.fusedLocationClient = fusedLocationClient;
		updateFabIcon(); // Set initial icon based on default state
	}

	// Called by Fragment when permission result is received
	public void onLocationPermissionResult(boolean granted) {
		_locationPermissionGranted.setValue(granted);
		if (granted) {
			// Permission granted, try to get location immediately
			requestLocationUpdate(); // Center camera after getting permission
		} else {
			// Permission denied, ensure tracking is off and icon is updated
			_isCameraTrackingUser.setValue(false);
		}
		updateFabIcon();
	}

	// Called by Fragment when FAB is clicked
	public void onMyLocationButtonClicked() {
		if (Boolean.TRUE.equals(_locationPermissionGranted.getValue())) {
			// Permission already granted, get location and center
			requestLocationUpdate();
		} else {
			// Permission not granted, trigger request via Fragment
			_requestPermissionEvent.setValue(new Event<>(true));
		}
	}

	// Called by Fragment when user starts moving the map
	public void onMapManualMoveStarted() {
		// If user moves map, stop tracking their location automatically
		if (Boolean.TRUE.equals(_isCameraTrackingUser.getValue())) {
			_isCameraTrackingUser.setValue(false);
			updateFabIcon();
		}
	}

	// --- Location Fetching ---
	@SuppressLint("MissingPermission") // Permission checked via _locationPermissionGranted state
	private void requestLocationUpdate() {
		if (!Boolean.TRUE.equals(_locationPermissionGranted.getValue())) {
			Log.w(TAG, "requestLocationUpdate called without permission.");
			_isCameraTrackingUser.setValue(false); // Ensure tracking is off
			updateFabIcon();
			return;
		}

		Log.d(TAG, "Requesting current location...");
		// Use PRIORITY_HIGH_ACCURACY for centering, adjust if needed
		CurrentLocationRequest request = new CurrentLocationRequest.Builder()
				.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
				.setDurationMillis(5000) // Timeout after 5 seconds
				.build();
		CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

		// Consider showing a loading state here
		_fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_location_searching);

		fusedLocationClient.getCurrentLocation(request, cancellationTokenSource.getToken())
				.addOnSuccessListener(location -> {
					if (location != null) {
						Log.d(TAG, "Location received: " + location.getLatitude() + ", " + location.getLongitude());
						LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
						_moveToLocationEvent.setValue(new Event<>(currentLatLng));
						_isCameraTrackingUser.setValue(true); // We just centered, so start tracking
						// Update icon AFTER potential camera move event
						updateFabIcon();
					} else {
						Log.w(TAG, "FusedLocationClient returned null location.");
						// Handle null location case (e.g., show error, reset icon)
						_isCameraTrackingUser.setValue(false); // Cannot track if location is null
						updateFabIcon();
					}
				})
				.addOnFailureListener(e -> {
					Log.e(TAG, "Failed to get current location", e);
					_isCameraTrackingUser.setValue(false); // Cannot track on failure
					updateFabIcon();
					// Handle failure (e.g., show error message)
				});
	}

	// --- Helper to update FAB icon based on current state ---
	private void updateFabIcon() {
		boolean permissionGranted = Boolean.TRUE.equals(_locationPermissionGranted.getValue());
		boolean trackingUser = Boolean.TRUE.equals(_isCameraTrackingUser.getValue());

		int iconRes;
		if (!permissionGranted) {
			iconRes = com.shoppr.core.ui.R.drawable.ic_location_disabled; // Permission denied/unknown
		} else if (trackingUser) {
			iconRes = com.shoppr.core.ui.R.drawable.ic_gps_fixed; // Permission granted AND centered
		} else {
			iconRes = com.shoppr.core.ui.R.drawable.ic_location_searching; // Permission granted but NOT centered
		}

		// Only update if changed
		if (_fabIconResId.getValue() == null || _fabIconResId.getValue() != iconRes) {
			_fabIconResId.setValue(iconRes);
		}
	}
}
