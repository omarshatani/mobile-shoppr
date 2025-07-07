package com.shoppr.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.shoppr.map.databinding.FragmentMapBinding;
import com.shoppr.model.Event;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.adapter.NearbyPostsAdapter;
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

	// Bottom Sheet components
	private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
	private NearbyPostsAdapter nearbyPostsAdapter;

	// The manager for map markers and clustering
	private PostClusterManager postClusterManager;

	private final ActivityResultLauncher<String> requestPermissionLauncher =
			registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				viewModel.onLocationPermissionResult(isGranted);
				if (!isGranted) {
					Toast.makeText(requireContext(), R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
				}
			});

	public MapFragment() {
	}

	@Override
	protected boolean shouldApplyBaseInsetPadding() {
		return false;
	}

	@Override
	protected boolean isLightStatusBarRequired() {
		return true;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(MapViewModel.class);
		if (getContext() != null) {
			viewModel.onLocationPermissionResult(hasFineLocationPermission());
		}
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

		setupBottomSheet();
		setupFabClickListener();
		applyBottomNavPadding(binding.getRoot());
		observeViewModel();
	}

	@Override
	public void onStart() {
		super.onStart();
		viewModel.onMapFragmentStarted();
	}

	@Override
	public void onStop() {
		super.onStop();
		viewModel.onMapFragmentStopped();
	}

	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		this.googleMap = googleMap;
		Log.d(TAG, "onMapReady called. Map is ready.");

		// Initialize the PostClusterManager
		if (getContext() != null) {
			postClusterManager = new PostClusterManager(getContext(), googleMap, post -> {
				// This is the callback for when a marker is clicked
				Log.i(TAG, "Marker clicked for post: " + post.getTitle() + " (ID: " + post.getId() + ")");
				Toast.makeText(getContext(), "Tapped on: " + post.getTitle(), Toast.LENGTH_SHORT).show();
				// TODO: Show a bottom sheet with this post's details or navigate
			});
		}

		googleMap.setOnCameraMoveStartedListener(this);
		updateMapMyLocationUI(viewModel.locationPermissionGranted.getValue());

		// Observe posts after map is ready
		viewModel.mapPosts.observe(getViewLifecycleOwner(), posts -> {
			if (postClusterManager != null) {
				Log.d(TAG, "Updating map with " + (posts != null ? posts.size() : 0) + " posts.");
				postClusterManager.setPosts(posts);
			}
		});
	}

	@Override
	public void onCameraMoveStarted(int reason) {
		if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
			Log.d(TAG, "User started moving the map manually.");
			viewModel.onMapManualMoveStarted();
		}
		// The PostClusterManager's internal onCameraIdleListener handles re-clustering.
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (googleMap != null) {
			googleMap.setOnCameraMoveStartedListener(null);
		}
		if (postClusterManager != null) {
			postClusterManager.cleanup(); // Clean up listeners
			postClusterManager = null;
		}
		googleMap = null;
		binding = null;
		Log.d(TAG, "onDestroyView called, resources cleaned up.");
	}

	private void setupBottomSheet() {
		bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetNearby);
		bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

		RecyclerView nearbyPostsRecyclerView = binding.bottomSheetNearby.findViewById(com.shoppr.core.ui.R.id.recycler_view_nearby_posts);
		nearbyPostsAdapter = new NearbyPostsAdapter(post -> {
			Toast.makeText(getContext(), "Tapped on list item: " + post.getTitle(), Toast.LENGTH_SHORT).show();
			// TODO: Navigate to post detail screen
		});
		nearbyPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		nearbyPostsRecyclerView.setAdapter(nearbyPostsAdapter);

		bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				String state = "";
				switch (newState) {
					case BottomSheetBehavior.STATE_COLLAPSED:
						state = "COLLAPSED";
						break;
					case BottomSheetBehavior.STATE_EXPANDED:
						state = "EXPANDED";
						break;
					case BottomSheetBehavior.STATE_HALF_EXPANDED:
						state = "HALF_EXPANDED";
						break;
					case BottomSheetBehavior.STATE_DRAGGING:
						state = "DRAGGING";
						break;
					case BottomSheetBehavior.STATE_SETTLING:
						state = "SETTLING";
						break;
					case BottomSheetBehavior.STATE_HIDDEN:
						state = "HIDDEN";
						break;
				}
				Log.d(TAG, "Bottom sheet state changed to: " + state);
			}
			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				moveFabWithBottomSheet(bottomSheet);
			}
		});

		binding.bottomSheetNearby.post(() -> moveFabWithBottomSheet(binding.bottomSheetNearby));
	}

	private void moveFabWithBottomSheet(@NonNull View bottomSheet) {
		if (binding == null || getContext() == null) return;
		final float fabHeight = binding.fabMyLocation.getHeight();
		if (fabHeight == 0) return;
		final float fabMargin = getResources().getDimension(com.shoppr.core.ui.R.dimen.fab_margin);
		float halfExpandedRatio = bottomSheetBehavior.getHalfExpandedRatio();
		float parentHeight = ((View) bottomSheet.getParent()).getHeight();
		float halfExpandedSheetTop = parentHeight * (1f - halfExpandedRatio);
		float fabStopY = halfExpandedSheetTop - fabHeight - fabMargin;
		float currentFabY = bottomSheet.getTop() - fabHeight - fabMargin;
		binding.fabMyLocation.setY(Math.max(fabStopY, currentFabY));
	}

	private void observeViewModel() {
		viewModel.locationPermissionGranted.observe(getViewLifecycleOwner(), this::updateMapMyLocationUI);

		viewModel.fabIconResId.observe(getViewLifecycleOwner(), iconResId -> {
			if (binding != null && iconResId != null) {
				binding.fabMyLocation.setImageResource(iconResId);
			}
		});

		viewModel.moveToLocationEvent.observe(getViewLifecycleOwner(), new Event.EventObserver<>(latLng -> {
			Log.d(TAG, "Received moveToLocationEvent: " + latLng);
			if (googleMap != null && latLng != null) {
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
			}
		}));

		viewModel.requestPermissionEvent.observe(getViewLifecycleOwner(), new Event.EventObserver<>(shouldRequest -> {
			Log.d(TAG, "Received requestPermissionEvent");
			if (shouldRequest) {
				requestLocationPermission();
			}
		}));

		viewModel.toastMessageEvent.observe(getViewLifecycleOwner(), new Event.EventObserver<>(message -> {
			if (getContext() != null && message != null) {
				Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
			}
		}));

		// The observer for mapPosts (for markers) is now in onMapReady.
		// This observer is for the bottom sheet's list.
		viewModel.mapPosts.observe(getViewLifecycleOwner(), posts -> {
			if (nearbyPostsAdapter != null) {
				Log.d(TAG, "Updating bottom sheet list with " + (posts != null ? posts.size() : 0) + " posts.");
				nearbyPostsAdapter.submitList(posts);
			}
		});
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
		}
	}

	private void requestLocationPermission() {
		Log.d(TAG, "Requesting location permission...");
		viewModel.onLocationSearching();
		requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
	}

	private boolean hasFineLocationPermission() {
		if (getContext() == null) return false;
		return ContextCompat.checkSelfPermission(
				requireContext(),
				Manifest.permission.ACCESS_FINE_LOCATION
		) == PackageManager.PERMISSION_GRANTED;
	}

	private void applyBottomNavPadding(View view) {
		ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
			InsetUtils.applyBottomNavPadding(
					v,
					windowInsets,
					com.shoppr.core.ui.R.dimen.bottom_nav_height + 16
			);
			return windowInsets;
		});
		ViewCompat.requestApplyInsets(view);
	}
}