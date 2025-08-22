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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.Cluster;
import com.shoppr.map.databinding.FragmentMapBinding;
import com.shoppr.model.Post;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.adapter.MapPostsCarouselAdapter;

import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MapFragment extends BaseFragment implements
		OnMapReadyCallback,
		GoogleMap.OnCameraMoveStartedListener,
		MapPostsCarouselAdapter.OnPostClickListener,
		MapPostsCarouselAdapter.OnFavoriteClickListener,
		MapPostsCarouselAdapter.OnMakeAnOfferClickListener,
		PostClusterManager.OnPostMarkerClickListener,
		PostClusterManager.OnPostClusterClickListener {

	private static final String TAG = "MapFragment";
	private FragmentMapBinding binding;
	private MapViewModel viewModel;
	private GoogleMap googleMap;
	private PostClusterManager postClusterManager;
	private MapPostsCarouselAdapter carouselAdapter;

	private final ActivityResultLauncher<String> requestPermissionLauncher =
			registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				viewModel.onLocationPermissionResult(isGranted);
				if (!isGranted) {
					Toast.makeText(requireContext(), R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
				}
			});

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
		SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
		if (mapFragment != null) {
			mapFragment.getMapAsync(this);
		}
		setupCarousel();
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
			postClusterManager = new PostClusterManager(getContext(), googleMap, this, this);
		}
		googleMap.setOnCameraMoveStartedListener(this);
		updateMapMyLocationUI(viewModel.locationPermissionGranted.getValue());
	}

	@Override
	public void onCameraMoveStarted(int reason) {
		if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
			viewModel.onMapManualMoveStarted();
		}
	}

	private void setupCarousel() {
		carouselAdapter = new MapPostsCarouselAdapter(this, this, this);
		binding.postsCarouselRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
		binding.postsCarouselRecyclerView.setAdapter(carouselAdapter);
		binding.postsCarouselRecyclerView.setItemAnimator(null);
		new PagerSnapHelper().attachToRecyclerView(binding.postsCarouselRecyclerView);

		binding.postsCarouselRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					View centerView = binding.postsCarouselRecyclerView.findChildViewUnder(recyclerView.getWidth() / 2f, recyclerView.getHeight() / 2f);
					if (centerView != null) {
						int position = binding.postsCarouselRecyclerView.getChildAdapterPosition(centerView);
						if (position != RecyclerView.NO_POSITION) {
							Post post = carouselAdapter.getCurrentList().get(position);
							viewModel.centerMapOnPost(post);
						}
					}
				}
			}
		});
	}

	private void observeViewModel() {
		// Main observer for posts, driving both the map and the carousel
		viewModel.getMapPosts().observe(getViewLifecycleOwner(), posts -> {
			if (postClusterManager != null) {
				postClusterManager.setPosts(posts);
			}
			carouselAdapter.submitList(posts);
			binding.postsCarouselRecyclerView.setVisibility(posts == null || posts.isEmpty() ? View.GONE : View.VISIBLE);
		});

		viewModel.currentUserProfileLiveData.observe(getViewLifecycleOwner(), user -> {
			if (user != null) {
				carouselAdapter.setCurrentUserId(user.getId());
				if (user.getFavoritePosts() != null) {
					carouselAdapter.setFavoritePostIds(user.getFavoritePosts());
				} else {
					carouselAdapter.setFavoritePostIds(Collections.emptyList());
				}
			} else {
				carouselAdapter.setCurrentUserId(null);
			}
		});

		viewModel.getMapCenterEvent().observe(getViewLifecycleOwner(), event -> {
			LatLng location = event.getContentIfNotHandled();
			if (location != null && googleMap != null) {
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
			}
		});

		viewModel.getScrollCarouselToPositionEvent().observe(getViewLifecycleOwner(), event -> {
			Integer position = event.getContentIfNotHandled();
			if (position != null) {
				binding.postsCarouselRecyclerView.smoothScrollToPosition(position);
			}
		});

		viewModel.locationPermissionGranted.observe(getViewLifecycleOwner(), this::updateMapMyLocationUI);
		viewModel.fabIconResId.observe(getViewLifecycleOwner(), binding.fabMyLocation::setImageResource);
		viewModel.requestPermissionEvent.observe(getViewLifecycleOwner(), event -> {
			if (event.getContentIfNotHandled() != null) requestPermission();
		});
		viewModel.getToastMessageEvent().observe(getViewLifecycleOwner(), event -> {
			String message = event.getContentIfNotHandled();
			if (message != null) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
		});
	}

	@Override
	public void onPostClick(Post post) {
		// Navigate to full detail screen (future implementation)
	}

	@Override
	public void onFavoriteClick(Post post) {
		viewModel.onFavoriteClicked(post);
	}

	@Override
	public void onMakeAnOfferClick(Post post) {
		// Create an instance of the bottom sheet, passing the post data
		MakeOfferBottomSheet bottomSheet = MakeOfferBottomSheet.newInstance(post);
		// Show the bottom sheet
		bottomSheet.show(getChildFragmentManager(), MakeOfferBottomSheet.TAG);
	}

	@Override
	public void onPostMarkerClicked(@NonNull Post post) {
		viewModel.onPostMarkerClicked(post);
	}

	@Override
	public void onSameLocationClusterClicked(@NonNull List<Post> posts) {
		viewModel.onClusterClicked(posts);
	}

	@Override
	public void onDifferentLocationClusterClicked(@NonNull Cluster<PostClusterItem> cluster) {
		if (googleMap == null) return;
		LatLngBounds.Builder builder = LatLngBounds.builder();
		for (PostClusterItem item : cluster.getItems()) {
			builder.include(item.getPosition());
		}
		googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100)); // 100 is padding
	}

	private void setupFabClickListener() {
		binding.fabMyLocation.setOnClickListener(v -> viewModel.onMyLocationButtonClicked());
	}

	@SuppressLint("MissingPermission")
	private void updateMapMyLocationUI(Boolean isGranted) {
		if (googleMap == null || isGranted == null) return;
		try {
			googleMap.setMyLocationEnabled(isGranted);
			googleMap.getUiSettings().setMyLocationButtonEnabled(false);
		} catch (SecurityException e) {
			Log.e(TAG, "SecurityException setting MyLocationEnabled", e);
		}
	}

	private void requestPermission() {
		requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
	}

	private boolean hasFineLocationPermission() {
		if (getContext() == null) return false;
		return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (postClusterManager != null) {
			postClusterManager.cleanup();
		}
		googleMap = null;
		postClusterManager = null;
		binding = null;
	}
}