package com.shoppr.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.shoppr.map.databinding.FragmentMapBinding;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.utils.Event;
import com.shoppr.ui.utils.InsetUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MapFragment extends BaseFragment implements OnMapReadyCallback,
		GoogleMap.OnCameraMoveStartedListener {
	private static final String TAG = "MapFragment";
	private FragmentMapBinding binding;
	private MapViewModel viewModel;
	private GoogleMap googleMap;
	private SupportMapFragment mapFragment;

	private final ActivityResultLauncher<String> requestPermissionLauncher =
			registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				Log.d(TAG, "Permission result received: " + isGranted);
				viewModel.onLocationPermissionResult(isGranted);
				if (!isGranted) {
					Toast.makeText(requireContext(), R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
				}
			});

	public MapFragment() {};

	@Override
	protected boolean shouldApplyBaseInsetPadding() {
		return false;
	}

	@Override
	protected boolean isLightStatusBarRequired() {
		return false;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(MapViewModel.class);
		viewModel.onLocationPermissionResult(hasFineLocationPermission());
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = FragmentMapBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
		if (mapFragment != null) {
			mapFragment.getMapAsync(this);
		} else {
			Log.e(TAG, "SupportMapFragment NOT found!");
		}

		setupFabClickListener();
		observeViewModel();
		setupRootViewInsets(binding.getRoot());
	}

	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		this.googleMap = googleMap;
		Log.d(TAG, "onMapReady called. Map is ready.");
		googleMap.setOnCameraMoveStartedListener(this);
		updateMapMyLocationUI(viewModel.locationPermissionGranted.getValue());
	}

	@Override
	public void onCameraMoveStarted(int reason) {
		if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
			Log.d(TAG, "User started moving the map manually.");
			viewModel.onMapManualMoveStarted();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (googleMap != null) {
			googleMap.setOnCameraMoveStartedListener(null);
		}
		googleMap = null;
		mapFragment = null;
		binding = null;
		Log.d(TAG, "onDestroyView called, binding set to null");
	}

	private void observeViewModel() {
		// Observe permission changes
		viewModel.locationPermissionGranted.observe(getViewLifecycleOwner(), this::updateMapMyLocationUI);

		// Observe FAB icon changes - use binding to access FAB
		viewModel.fabIconResId.observe(getViewLifecycleOwner(), iconResId -> {
			if (binding != null && iconResId != null) {
				binding.fabMyLocation.setImageResource(iconResId);
			}
		});

		// Observe camera move events
		viewModel.moveToLocationEvent.observe(getViewLifecycleOwner(), new Event.EventObserver<>(latLng -> {
			Log.d(TAG, "Received moveToLocationEvent: " + latLng);
			if (googleMap != null && latLng != null) {
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
			}
			return null;
		}));

		// Observe permission request events
		viewModel.requestPermissionEvent.observe(getViewLifecycleOwner(), new Event.EventObserver<>(shouldRequest -> {
			Log.d(TAG, "Received requestPermissionEvent");
			if (shouldRequest) {
				requestLocationPermission();
			}
			return null;
		}));
	}

	@SuppressLint("MissingPermission")
	private void updateMapMyLocationUI(Boolean isGranted) {
		if (googleMap == null) return;
		try {
			if (Boolean.TRUE.equals(isGranted)) {
				googleMap.setMyLocationEnabled(true);
				googleMap.getUiSettings().setMyLocationButtonEnabled(false);
				Log.d(TAG, "Map MyLocation layer enabled.");
			} else {
				googleMap.setMyLocationEnabled(false);
				googleMap.getUiSettings().setMyLocationButtonEnabled(false);
				Log.d(TAG, "Map MyLocation layer disabled.");
			}
		} catch (SecurityException e) {
			Log.e(TAG, "SecurityException setting MyLocationEnabled", e);
		}
	}

	private void setupFabClickListener() {
		if (binding != null) {
			binding.fabMyLocation.setOnClickListener(v -> {
				Log.d(TAG, "My Location FAB clicked");
				viewModel.onMyLocationButtonClicked();
			});
		} else {
			Log.w(TAG, "FAB was null during setupFabClickListener");
		}
	}

	private void requestLocationPermission() {
		Log.d(TAG, "Requesting location permission...");
		requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
	}

	private boolean hasFineLocationPermission() {
		return ContextCompat.checkSelfPermission(
				requireContext(),
				Manifest.permission.ACCESS_FINE_LOCATION
		) == PackageManager.PERMISSION_GRANTED;
	}

	private void setupRootViewInsets(View view) {
		ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
			InsetUtils.applyBottomNavPadding(
					v,
					windowInsets,
					com.shoppr.core.ui.R.dimen.bottom_nav_height
			);
			return windowInsets;
		});
		ViewCompat.requestApplyInsets(view);
	}

}