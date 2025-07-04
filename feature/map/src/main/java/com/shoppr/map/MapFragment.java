package com.shoppr.map;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;

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
import com.google.maps.android.clustering.ClusterManager;
import com.shoppr.map.databinding.FragmentMapBinding;
import com.shoppr.model.Event;
import com.shoppr.model.Post;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.adapter.NearbyPostsAdapter;
import com.shoppr.ui.utils.InsetUtils;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MapFragment extends BaseFragment implements OnMapReadyCallback,
		GoogleMap.OnCameraMoveStartedListener, ClusterManager.OnClusterItemClickListener<PostClusterItem> {
	private static final String TAG = "MapFragment";
	private FragmentMapBinding binding;
	private MapViewModel viewModel;
	private GoogleMap googleMap;
	private SupportMapFragment mapFragment;
	private ClusterManager<PostClusterItem> clusterManager;
	private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
	private RecyclerView nearbyPostsRecyclerView;
	private NearbyPostsAdapter nearbyPostsAdapter;

	private final ActivityResultLauncher<String> requestPermissionLauncher =
			registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				Log.d(TAG, "Permission result received: " + isGranted);
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
		// Initial permission check
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
		observeViewModel();
		setupRootViewInsets(binding.getRoot());
		setupFabClickListener();
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

		// Initialize ClusterManager
		if (getContext() != null) {
			clusterManager = new ClusterManager<>(getContext(), googleMap);
			// Optional: Customize the clustering algorithm
			// clusterManager.setAlgorithm(new NonHierarchicalDistanceBasedAlgorithm<>());
			// Optional: Customize the renderer for individual markers and clusters
			// clusterManager.setRenderer(new YourCustomClusterRenderer(getContext(), googleMap, clusterManager));

			googleMap.setOnCameraIdleListener(clusterManager); // Important for clustering to work
			googleMap.setOnMarkerClickListener(clusterManager); // Delegate marker clicks to ClusterManager
			clusterManager.setOnClusterItemClickListener(this); // Listen for clicks on individual items
		}


		googleMap.setOnCameraMoveStartedListener(this);
		updateMapMyLocationUI(viewModel.locationPermissionGranted.getValue());

		// Observe posts after map is ready and cluster manager is set up
		viewModel.mapPosts.observe(getViewLifecycleOwner(), posts -> {
			Log.d(TAG, "Observed map posts. Count: " + (posts != null ? posts.size() : 0));
			addPostsToMap(posts);
		});
	}

	@Override
	public void onCameraMoveStarted(int reason) {
		if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
			Log.d(TAG, "User started moving the map manually.");
			viewModel.onMapManualMoveStarted();
		}
		if (clusterManager != null) {
			// Also notify cluster manager on general camera move if not covered by onCameraIdle
			clusterManager.onCameraIdle();
		}
	}

	@Override
	public boolean onClusterItemClick(@NonNull PostClusterItem item) {
		Log.i(TAG, "Clicked marker for post: " + item.getTitle() + " (ID: " + item.post.getId() + ")");
		Toast.makeText(getContext(), "Tapped on: " + item.getTitle(), Toast.LENGTH_SHORT).show();
		// TODO: Navigate to Post Detail screen or show BottomSheet
		// Example: NavHostFragment.findNavController(this).navigate(
		//    MapFragmentDirections.actionMapFragmentToPostDetailFragment(item.post.getId())
		// );
		return false; // Return false to allow default behavior (show info window & center on marker)
		// Return true if you've fully handled the click.
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (googleMap != null) {
			googleMap.setOnCameraMoveStartedListener(null);
			googleMap.setOnCameraIdleListener(null);
			googleMap.setOnMarkerClickListener(null);
		}
		if (clusterManager != null) {
			clusterManager.setOnClusterItemClickListener(null);
			clusterManager.clearItems(); // Clear items to avoid memory leaks
		}
		clusterManager = null;
		googleMap = null;
		mapFragment = null; // mapFragment is managed by childFragmentManager, usually okay
		binding = null;
		Log.d(TAG, "onDestroyView called, map resources cleaned up.");
	}

	private void setupFabClickListener() {
		if (binding != null) {
			binding.fabMyLocation.setOnClickListener(v -> {
				Log.d(TAG, "My Location FAB clicked");
				viewModel.onMyLocationButtonClicked();
			});
		}
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
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
			}
		}));

		viewModel.requestPermissionEvent.observe(getViewLifecycleOwner(), new Event.EventObserver<>(shouldRequest -> {
			Log.d(TAG, "Received requestPermissionEvent");
			if (shouldRequest) {
				requestLocationPermission();
			}
		}));

		viewModel.toastMessageEvent.observe(getViewLifecycleOwner(), new Event.EventObserver<>(message -> {
			if (getContext() != null && message != null && !message.isEmpty()) {
				Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
			}
		}));
	}

	private void setupBottomSheet() {
		final float fabHeight = binding.fabMyLocation.getHeight();
		// The view with the behavior is the LinearLayout with id 'bottom_sheet_nearby'
		bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetNearby);

		// Set initial state to collapsed (peek height)
		bottomSheetBehavior.setState(STATE_COLLAPSED);
		bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(com.shoppr.core.ui.R.dimen.bottom_sheet_peek_height));

		// Find the RecyclerView inside the bottom sheet layout
		RecyclerView nearbyPostsRecyclerView = binding.bottomSheetNearby.findViewById(com.shoppr.core.ui.R.id.recycler_view_nearby_posts);
		nearbyPostsAdapter = new NearbyPostsAdapter(post -> {
			Toast.makeText(getContext(), "Tapped on " + post.getTitle(), Toast.LENGTH_SHORT).show();
			// TODO: Navigate to post detail screen
		});
		nearbyPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		nearbyPostsRecyclerView.setAdapter(nearbyPostsAdapter);

		// Add a callback to listen for state changes and slide events
		bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				moveFabWithBottomSheet(bottomSheet);
			}
		});

		binding.bottomSheetNearby.post(() -> {
			Log.d(TAG, "Setting initial FAB position.");
			// The initial slideOffset for a collapsed sheet is 0.
			moveFabWithBottomSheet(binding.bottomSheetNearby);
		});
	}

	/**
	 * Calculates and sets the FAB's vertical position based on the bottom sheet's slide offset.
	 * The FAB follows the sheet up to the half-expanded point, then stays fixed.
	 * @param bottomSheet The bottom sheet view.
	 */
	private void moveFabWithBottomSheet(@NonNull View bottomSheet) {
		if (binding == null || getContext() == null) return;

		final float fabHeight = binding.fabMyLocation.getHeight();
		if (fabHeight == 0) return; // Wait until FAB is measured

		final float fabMargin = 16;
		final float parentHeight = ((View) bottomSheet.getParent()).getHeight();
		final float halfExpandedRatio = bottomSheetBehavior.getHalfExpandedRatio();

		// Calculate the Y position where the FAB should "stop" moving up.
		// This is its position when the sheet is at its half-expanded state.
		float halfExpandedSheetTop = parentHeight * (1f - halfExpandedRatio);
		float fabStopY = halfExpandedSheetTop - fabHeight - fabMargin;

		// Calculate the FAB's current "natural" position if it were to follow the sheet
		float currentFabY = bottomSheet.getTop() - fabHeight - fabMargin;

		// The FAB should follow the sheet up to the stop point, and then stay there.
		// We use Math.max because Y coordinates decrease as you go up the screen.
		// We want the FAB's Y to be the *larger* of its current calculated position and its stop position.
		// As the sheet goes up, bottomSheet.getTop() decreases, so currentFabY decreases.
		// Math.max will choose currentFabY until it becomes smaller than fabStopY, at which point
		// it will always choose fabStopY, effectively "pinning" the FAB.
		binding.fabMyLocation.setY(Math.max(fabStopY, currentFabY));
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

	@SuppressLint("MissingPermission")
	private void updateMapMyLocationUI(Boolean isGranted) {
		if (googleMap == null) return;
		try {
			if (Boolean.TRUE.equals(isGranted)) {
				googleMap.setMyLocationEnabled(true);
				googleMap.getUiSettings().setMyLocationButtonEnabled(false); // Using custom FAB
				Log.d(TAG, "Map MyLocation layer enabled.");
			} else {
				googleMap.setMyLocationEnabled(false);
				Log.d(TAG, "Map MyLocation layer disabled.");
			}
		} catch (SecurityException e) {
			Log.e(TAG, "SecurityException setting MyLocationEnabled", e);
		}
	}

	private void requestLocationPermission() {
		Log.d(TAG, "Requesting location permission...");
		if (viewModel != null) { // Check if viewModel is initialized
			viewModel.onLocationSearching(); // Update FAB icon
		}
		requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
	}

	private boolean hasFineLocationPermission() {
		if (getContext() == null) return false;
		return ContextCompat.checkSelfPermission(
				requireContext(),
				Manifest.permission.ACCESS_FINE_LOCATION
		) == PackageManager.PERMISSION_GRANTED;
	}

	private void addPostsToMap(@Nullable List<Post> posts) {
		if (googleMap == null || clusterManager == null) {
			Log.w(TAG, "GoogleMap or ClusterManager not ready, cannot add posts.");
			return;
		}

		clusterManager.clearItems(); // Clear previous markers before adding new ones

		if (posts == null || posts.isEmpty()) {
			Log.d(TAG, "No posts to display on map.");
			clusterManager.cluster(); // Important to call cluster even if empty to refresh map
			return;
		}

		Log.d(TAG, "Adding " + posts.size() + " posts to map.");
		for (Post post : posts) {
			if (post.getLatitude() != null && post.getLongitude() != null) {
				String title = post.getTitle() != null ? post.getTitle() : "Untitled Post";
				String snippet = post.getCategory() != null ? "Category: " + post.getCategory() :
						(post.getPrice() != null ? "Price: " + post.getPrice() : "View Details");

				PostClusterItem clusterItem = new PostClusterItem(
						post.getLatitude(),
						post.getLongitude(),
						title,
						snippet,
						post // Store the original post object
				);
				clusterManager.addItem(clusterItem);
			} else {
				Log.w(TAG, "Post with ID " + post.getId() + " has no location data.");
			}
		}
		clusterManager.cluster(); // Re-cluster after adding new items
	}


}