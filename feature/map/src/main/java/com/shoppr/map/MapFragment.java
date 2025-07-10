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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.maps.android.clustering.Cluster;
import com.shoppr.core.ui.databinding.BottomSheetContentPostDetailBinding;
import com.shoppr.map.databinding.FragmentMapBinding;
import com.shoppr.model.Event;
import com.shoppr.model.Post;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.adapter.NearbyPostsAdapter;
import com.shoppr.ui.utils.ImageLoader;

import java.util.List;

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
    private PostClusterManager postClusterManager;
    private View nearbyListView;
    private View postDetailView;
    private BottomSheetContentPostDetailBinding detailViewBinding;

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
            postClusterManager = new PostClusterManager(getContext(), googleMap,
                    // OnPostMarkerClickListener
                    post -> viewModel.onPostMarkerClicked(post.getId()),
                    // OnPostClusterClickListener
                    new PostClusterManager.OnPostClusterClickListener() {
                        @Override
                        public void onSameLocationClusterClicked(@NonNull List<Post> posts) {
                            Log.d(TAG, "Handling click for cluster with items at same location.");
                            viewModel.onSameLocationClusterClicked(posts);
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                        }

                        @Override
                        public void onDifferentLocationClusterClicked(@NonNull Cluster<PostClusterItem> cluster) {
                            Log.d(TAG, "Handling click for cluster with items at different locations (zooming).");
                            // Build bounds for all items in cluster and animate camera
                            LatLngBounds.Builder builder = LatLngBounds.builder();
                            for (PostClusterItem item : cluster.getItems()) {
                                builder.include(item.getPosition());
                            }
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100)); // 100 is padding
                        }
                    }
            );
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
        bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(com.shoppr.core.ui.R.dimen.bottom_sheet_peek_height));

        // Get references to the two main views inside the bottom sheet
        nearbyListView = binding.bottomSheetNearby.findViewById(com.shoppr.core.ui.R.id.view_nearby_list_container);
        postDetailView = binding.bottomSheetNearby.findViewById(com.shoppr.core.ui.R.id.view_post_detail_container);
        // Bind the detail view for easy access to its children
        detailViewBinding = BottomSheetContentPostDetailBinding.bind(postDetailView);


        RecyclerView nearbyPostsRecyclerView = nearbyListView.findViewById(com.shoppr.core.ui.R.id.recycler_view_nearby_posts);
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

        // Observe the main list of posts for the map
        viewModel.mapPosts.observe(getViewLifecycleOwner(), posts -> {
            if (postClusterManager != null) {
                postClusterManager.setPosts(posts); // Update markers on map
            }
            if (nearbyPostsAdapter != null) {
                nearbyPostsAdapter.submitList(posts); // Update list in bottom sheet
            }
        });


        viewModel.selectedPostDetails.observe(getViewLifecycleOwner(), selectedPost -> {
            if (selectedPost != null) {
                // A post is selected, show the detail view
                nearbyListView.setVisibility(View.GONE);
                postDetailView.setVisibility(View.VISIBLE);
                bindPostDetail(selectedPost);
                // Expand the bottom sheet to show the details if it's collapsed
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                }
            } else {
                // No post is selected, show the "nearby" list view
                nearbyListView.setVisibility(View.VISIBLE);
                postDetailView.setVisibility(View.GONE);
            }
        });

        viewModel.isDetailLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Show a loading indicator in the bottom sheet's detail view
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

    private void bindPostDetail(@NonNull Post post) {
        detailViewBinding.detailPostTitle.setText(post.getTitle());
        detailViewBinding.detailPostPrice.setText(post.getPrice());
        detailViewBinding.detailPostDescription.setText(post.getDescription());

        if (post.getLister() != null) {
            detailViewBinding.detailPostListerInfo.setText("by " + post.getLister().getName());
            detailViewBinding.detailPostListerInfo.setVisibility(View.VISIBLE);
        } else {
            detailViewBinding.detailPostListerInfo.setVisibility(View.GONE);
        }

        // Use the ImageLoader utility from your core:ui module
        String imageUrl = (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) ? post.getImageUrl().get(0) : null;
        ImageLoader.loadImage(detailViewBinding.detailPostImage, imageUrl);

        detailViewBinding.buttonViewFullPost.setOnClickListener(v -> {
            // TODO: Navigate to the full PostDetailFragment
            Toast.makeText(getContext(), "TODO: Navigate to full detail page", Toast.LENGTH_SHORT).show();
        });
    }
}