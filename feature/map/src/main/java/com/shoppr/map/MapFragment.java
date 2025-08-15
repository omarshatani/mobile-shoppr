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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.maps.android.clustering.Cluster;
import com.shoppr.core.ui.R;
import com.shoppr.core.ui.databinding.BottomSheetContentPostDetailBinding;
import com.shoppr.map.databinding.FragmentMapBinding;
import com.shoppr.model.Event;
import com.shoppr.model.Post;
import com.shoppr.navigation.BottomNavManager;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.adapter.NearbyPostsAdapter;
import com.shoppr.ui.utils.ImageLoader;

import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MapFragment extends BaseFragment implements OnMapReadyCallback,
		GoogleMap.OnCameraMoveStartedListener, NearbyPostsAdapter.OnFavoriteClickListener {
	private static final String TAG = "MapFragment";
	private FragmentMapBinding binding;
	private MapViewModel viewModel;
	private GoogleMap googleMap;
	private SupportMapFragment mapFragment;

	private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
	private NearbyPostsAdapter nearbyPostsAdapter;

	private View nearbyListView;
	private View postDetailView;
	private View emptyStateNearbyView;

	private BottomSheetContentPostDetailBinding detailViewBinding;

	private PostClusterManager postClusterManager;

	private final ActivityResultLauncher<String> requestPermissionLauncher =
			registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				viewModel.onLocationPermissionResult(isGranted);
				if (!isGranted) {
					Toast.makeText(requireContext(), com.shoppr.map.R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
				}
			});

	public MapFragment() {
	}

	@Override
	protected boolean isLightStatusBarRequired() {
		return true;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(MapViewModel.class);
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

		ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
			Insets systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
			BottomSheetBehavior.from(binding.bottomSheetNearby).setExpandedOffset(systemBarInsets.top);
			return windowInsets;
		});

		mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(com.shoppr.map.R.id.map);
		if (mapFragment != null) {
			mapFragment.getMapAsync(this);
		}
		setupBottomSheet();
		setupFabClickListener();
		observeViewModel();
	}

	@Override
	protected InsetType getInsetType() {
		return InsetType.NONE;
	}

	@Override
	public void onStart() {
		super.onStart();
		viewModel.onLocationPermissionResult(hasFineLocationPermission());
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
		if (getContext() != null) {
			postClusterManager = new PostClusterManager(getContext(), googleMap,
					post -> viewModel.onPostMarkerClicked(post.getId()),
					new PostClusterManager.OnPostClusterClickListener() {
						@Override
						public void onSameLocationClusterClicked(@NonNull List<Post> posts) {
							viewModel.onSameLocationClusterClicked(posts);
							bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
						}

						@Override
						public void onDifferentLocationClusterClicked(@NonNull Cluster<PostClusterItem> cluster) {
							LatLngBounds.Builder builder = LatLngBounds.builder();
							for (PostClusterItem item : cluster.getItems()) {
								builder.include(item.getPosition());
							}
							googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
						}
					}
			);
		}
		googleMap.setOnCameraMoveStartedListener(this);
		updateMapMyLocationUI(viewModel.locationPermissionGranted.getValue());
		viewModel.mapPosts.observe(getViewLifecycleOwner(), posts -> {
			if (postClusterManager != null) {
				postClusterManager.setPosts(posts);
			}
		});
	}

	@Override
	public void onCameraMoveStarted(int reason) {
		if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
			viewModel.onMapManualMoveStarted();
		}
	}

	@Override
	public void onFavoriteClick(Post post) {
		viewModel.onFavoriteClicked(post);
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (googleMap != null) {
			googleMap.setOnCameraMoveStartedListener(null);
		}
		if (postClusterManager != null) {
			postClusterManager.cleanup();
			postClusterManager = null;
		}
		googleMap = null;
		binding = null;
	}

	private void setupBottomSheet() {
		bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetNearby);
		bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(com.shoppr.core.ui.R.dimen.bottom_sheet_peek_height));

		nearbyListView = binding.bottomSheetNearby.findViewById(com.shoppr.core.ui.R.id.view_nearby_list_container);
		postDetailView = binding.bottomSheetNearby.findViewById(com.shoppr.core.ui.R.id.view_post_detail_container);
		detailViewBinding = BottomSheetContentPostDetailBinding.bind(postDetailView);
		emptyStateNearbyView = nearbyListView.findViewById(com.shoppr.core.ui.R.id.layout_empty_state_nearby);

		RecyclerView nearbyPostsRecyclerView = nearbyListView.findViewById(com.shoppr.core.ui.R.id.recycler_view_nearby_posts);

		nearbyPostsAdapter = new NearbyPostsAdapter(post -> {
			viewModel.onPostMarkerClicked(post.getId());
			BottomNavManager manager = findParentBottomNavManager();
			if (manager != null) {
				manager.setBottomNavVisibility(false);
			}
			bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
		}, this);

		nearbyPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		nearbyPostsRecyclerView.setAdapter(nearbyPostsAdapter);

		bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
					BottomNavManager manager = findParentBottomNavManager();
					if (manager != null) {
						manager.setBottomNavVisibility(true);
					}
					viewModel.clearSelectedPost();
				}
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
		viewModel.currentUserProfileLiveData.observe(getViewLifecycleOwner(), user -> {
			if (user != null && user.getFavoritePosts() != null) {
				nearbyPostsAdapter.setFavoritePostIds(user.getFavoritePosts());
			} else {
				nearbyPostsAdapter.setFavoritePostIds(Collections.emptyList());
			}
		});
		viewModel.locationPermissionGranted.observe(getViewLifecycleOwner(), this::updateMapMyLocationUI);
		viewModel.fabIconResId.observe(getViewLifecycleOwner(), iconResId -> {
			if (binding != null && iconResId != null) {
				binding.fabMyLocation.setImageResource(iconResId);
			}
		});
		viewModel.moveToLocationEvent.observe(getViewLifecycleOwner(), new Event.EventObserver<>(latLng -> {
			if (googleMap != null && latLng != null) {
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
			}
		}));
		viewModel.requestPermissionEvent.observe(getViewLifecycleOwner(), new Event.EventObserver<>(shouldRequest -> {
			if (shouldRequest) {
				requestLocationPermission();
			}
		}));
		viewModel.toastMessageEvent.observe(getViewLifecycleOwner(), new Event.EventObserver<>(message -> {
			if (getContext() != null && message != null) {
				Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
			}
		}));
		viewModel.bottomSheetPosts.observe(getViewLifecycleOwner(), posts -> {
			if (nearbyPostsAdapter != null) {
				nearbyPostsAdapter.submitList(posts);
				RecyclerView nearbyRecyclerView = nearbyListView.findViewById(com.shoppr.core.ui.R.id.recycler_view_nearby_posts);
				if (posts == null || posts.isEmpty()) {
					nearbyRecyclerView.setVisibility(View.GONE);
					emptyStateNearbyView.setVisibility(View.VISIBLE);
				} else {
					nearbyRecyclerView.setVisibility(View.VISIBLE);
					emptyStateNearbyView.setVisibility(View.GONE);
				}
			}
		});
		viewModel.selectedPostDetails.observe(getViewLifecycleOwner(), selectedPost -> {
			if (selectedPost != null) {
				nearbyListView.setVisibility(View.GONE);
				binding.bottomSheetNearby.findViewById(com.shoppr.core.ui.R.id.text_bottom_sheet_title).setVisibility(View.GONE);
				postDetailView.setVisibility(View.VISIBLE);
				bindPostDetail(selectedPost);
				if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
					bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
				}
			} else {
				nearbyListView.setVisibility(View.VISIBLE);
				binding.bottomSheetNearby.findViewById(com.shoppr.core.ui.R.id.text_bottom_sheet_title).setVisibility(View.VISIBLE);
				postDetailView.setVisibility(View.GONE);
			}
		});
		viewModel.isFavorite().observe(getViewLifecycleOwner(), isFavorite -> {
			if (detailViewBinding != null && isFavorite != null) {
				detailViewBinding.buttonFavorite.setImageResource(
						isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline
				);
			}
		});
	}

	private void bindPostDetail(@NonNull Post post) {
		detailViewBinding.detailPostTitle.setText(post.getTitle());
		detailViewBinding.detailPostPrice.setText(String.format("%s %s", post.getPrice(), post.getCurrency()));
		detailViewBinding.detailPostDescription.setText(post.getDescription());

		detailViewBinding.chipsContainer.removeAllViews();
		List<String> categories = post.getCategories();
		if (categories != null && !categories.isEmpty()) {
			detailViewBinding.chipsContainer.setVisibility(View.VISIBLE);
			for (String categoryName : categories) {
				Chip chip = new Chip(getContext());
				chip.setText(categoryName);
				detailViewBinding.chipsContainer.addView(chip);
			}
		} else {
			detailViewBinding.chipsContainer.setVisibility(View.GONE);
		}

		if (post.getLister() != null) {
			detailViewBinding.detailListerName.setText(String.format("by %s", post.getLister().getName()));
			detailViewBinding.detailListerName.setVisibility(View.VISIBLE);
		} else {
			detailViewBinding.detailListerName.setVisibility(View.GONE);
		}

		String imageUrl = (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) ? post.getImageUrl().get(0) : null;
		ImageLoader.loadImage(detailViewBinding.detailPostImage, imageUrl);

		detailViewBinding.buttonFavorite.setOnClickListener(v -> viewModel.onFavoriteClicked(post));
	}

	@SuppressLint("MissingPermission")
	private void updateMapMyLocationUI(Boolean isGranted) {
		if (googleMap == null) return;
		try {
			if (Boolean.TRUE.equals(isGranted)) {
				googleMap.setMyLocationEnabled(true);
				googleMap.getUiSettings().setMyLocationButtonEnabled(false);
			} else {
				googleMap.setMyLocationEnabled(false);
			}
		} catch (SecurityException e) {
			Log.e(TAG, "SecurityException setting MyLocationEnabled", e);
		}
	}

	private void setupFabClickListener() {
		if (binding != null) {
			binding.fabMyLocation.setOnClickListener(v -> viewModel.onMyLocationButtonClicked());
		}
	}

	private void requestLocationPermission() {
		viewModel.onLocationSearching();
		requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
	}

	private boolean hasFineLocationPermission() {
		if (getContext() == null) return false;
		return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}
}