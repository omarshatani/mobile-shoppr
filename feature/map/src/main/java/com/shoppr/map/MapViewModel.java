package com.shoppr.map;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.shoppr.domain.usecase.GetCurrentDeviceLocationUseCase;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetMapPostsUseCase;
import com.shoppr.domain.usecase.ToggleFavoriteUseCase;
import com.shoppr.domain.usecase.UpdateUserDefaultLocationUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.Post;
import com.shoppr.model.User;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MapViewModel extends AndroidViewModel {
	private static final String TAG = "MapViewModel";

	private final GetCurrentUserUseCase getCurrentUserUseCase;
	private final GetCurrentDeviceLocationUseCase getCurrentDeviceLocationUseCase;
	private final UpdateUserDefaultLocationUseCase updateUserDefaultLocationUseCase;
	private final GetMapPostsUseCase getMapPostsUseCase;
	private final ToggleFavoriteUseCase toggleFavoriteUseCase;
	public final LiveData<User> currentUserProfileLiveData;

	private final MediatorLiveData<List<Post>> _mapPosts = new MediatorLiveData<>();

	public LiveData<List<Post>> getMapPosts() {
		return _mapPosts;
	}

	private final MutableLiveData<Event<Integer>> _scrollCarouselToPositionEvent = new MutableLiveData<>();

	public LiveData<Event<Integer>> getScrollCarouselToPositionEvent() {
		return _scrollCarouselToPositionEvent;
	}

	private final MutableLiveData<Event<LatLng>> _mapCenterEvent = new MutableLiveData<>();

	public LiveData<Event<LatLng>> getMapCenterEvent() {
		return _mapCenterEvent;
	}

	private final MutableLiveData<Boolean> _locationPermissionGranted = new MutableLiveData<>(false);
	public LiveData<Boolean> locationPermissionGranted = _locationPermissionGranted;

	private final MutableLiveData<Integer> _fabIconResId = new MutableLiveData<>(com.shoppr.core.ui.R.drawable.ic_gps_fixed);
	public LiveData<Integer> fabIconResId = _fabIconResId;

	private final MutableLiveData<Event<Boolean>> _requestPermissionEvent = new MutableLiveData<>();
	public LiveData<Event<Boolean>> requestPermissionEvent = _requestPermissionEvent;

	private final MutableLiveData<Event<String>> _toastMessageEvent = new MutableLiveData<>();

	public LiveData<Event<String>> getToastMessageEvent() {
		return _toastMessageEvent;
	}

	private boolean isMapManuallyMoved = false;
	private boolean initialMapCenterAttempted = false;
	private LiveData<List<Post>> currentPostsSource = null;

	@Inject
	public MapViewModel(@NonNull Application application,
											GetCurrentUserUseCase getCurrentUserUseCase,
											GetCurrentDeviceLocationUseCase getCurrentDeviceLocationUseCase,
											UpdateUserDefaultLocationUseCase updateUserDefaultLocationUseCase,
											GetMapPostsUseCase getMapPostsUseCase,
											ToggleFavoriteUseCase toggleFavoriteUseCase) {
		super(application);
		this.getCurrentUserUseCase = getCurrentUserUseCase;
		this.getCurrentDeviceLocationUseCase = getCurrentDeviceLocationUseCase;
		this.updateUserDefaultLocationUseCase = updateUserDefaultLocationUseCase;
		this.getMapPostsUseCase = getMapPostsUseCase;
		this.toggleFavoriteUseCase = toggleFavoriteUseCase;
		this.currentUserProfileLiveData = this.getCurrentUserUseCase.getFullUserProfile();

		_mapPosts.addSource(currentUserProfileLiveData, user -> {
			loadPostsForMap(user != null ? user.getId() : null);
			if (user != null) {
				if (!initialMapCenterAttempted && user.getLatitude() != null && user.getLongitude() != null) {
					_mapCenterEvent.postValue(new Event<>(new LatLng(user.getLatitude(), user.getLongitude())));
					initialMapCenterAttempted = true;
				} else if (!initialMapCenterAttempted) {
					if (Boolean.TRUE.equals(_locationPermissionGranted.getValue())) {
						fetchAndSaveDeviceLocation(true);
					}
					initialMapCenterAttempted = true;
				}
			} else {
				initialMapCenterAttempted = false;
			}
		});
	}

	private void loadPostsForMap(@Nullable String currentUserId) {
		if (currentPostsSource != null) {
			_mapPosts.removeSource(currentPostsSource);
		}
		currentPostsSource = getMapPostsUseCase.execute(currentUserId);
		_mapPosts.addSource(currentPostsSource, _mapPosts::setValue);
	}

	public void onMapFragmentStarted() {
		getCurrentUserUseCase.startObserving();
	}

	public void onMapFragmentStopped() {
		getCurrentUserUseCase.stopObserving();
	}

	public void onLocationPermissionResult(boolean isGranted) {
		_locationPermissionGranted.setValue(isGranted);
		if (isGranted) {
			_fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_gps_fixed);
			fetchAndSaveDeviceLocation(!initialMapCenterAttempted);
		} else {
			_fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_location_disabled);
		}
	}

	public void onMyLocationButtonClicked() {
		if (Boolean.TRUE.equals(_locationPermissionGranted.getValue())) {
			isMapManuallyMoved = false;
			initialMapCenterAttempted = false;
			fetchAndSaveDeviceLocation(true);
		} else {
			_requestPermissionEvent.setValue(new Event<>(true));
		}
	}

	public void onMapManualMoveStarted() {
		isMapManuallyMoved = true;
	}

	public void onPostMarkerClicked(Post post) {
		List<Post> currentPosts = _mapPosts.getValue();
		if (currentPosts != null) {
			int position = currentPosts.indexOf(post);
			if (position != -1) {
				_scrollCarouselToPositionEvent.setValue(new Event<>(position));
			}
		}
		centerMapOnPost(post);
	}

	public void onClusterClicked(List<Post> posts) {
		// Since the carousel always shows all posts, there's nothing to update here.
		// The cluster click in the fragment will handle zooming.
	}

	public void centerMapOnPost(Post post) {
		if (post.getLatitude() != null && post.getLongitude() != null) {
			_mapCenterEvent.setValue(new Event<>(new LatLng(post.getLatitude(), post.getLongitude())));
		}
	}

	public void onFavoriteClicked(Post post) {
		if (post == null || post.getId() == null) return;
		toggleFavoriteUseCase.execute(post.getId(), new ToggleFavoriteUseCase.FavoriteToggleCallbacks() {
			@Override
			public void onSuccess() {
			}

			@Override
			public void onError(@NonNull String message) {
				_toastMessageEvent.postValue(new Event<>(message));
			}
		});
	}

	private void fetchAndSaveDeviceLocation(boolean forceMapMove) {
		getCurrentDeviceLocationUseCase.execute(new GetCurrentDeviceLocationUseCase.GetDeviceLocationCallbacks() {
			@Override
			public void onDeviceLocationSuccess(@NonNull GetCurrentDeviceLocationUseCase.DeviceLocation deviceLocation) {
				if (forceMapMove || !isMapManuallyMoved || !initialMapCenterAttempted) {
					_mapCenterEvent.postValue(new Event<>(new LatLng(deviceLocation.latitude, deviceLocation.longitude)));
					initialMapCenterAttempted = true;
					isMapManuallyMoved = false;
				}
				updateUserDefaultLocationUseCase.execute(deviceLocation.latitude, deviceLocation.longitude, deviceLocation.address, new UpdateUserDefaultLocationUseCase.UpdateLocationCallbacks() {
					@Override
					public void onLocationUpdateSuccess() {

					}

					@Override
					public void onLocationUpdateError(@NonNull String message) {
						_toastMessageEvent.postValue(new Event<>("Could not save current location."));
					}
				});
			}

			@Override
			public void onDeviceLocationError(@NonNull String message) {
				if (!message.toLowerCase().contains("permission")) {
					_toastMessageEvent.postValue(new Event<>("Could not get current location: " + message));
				}
			}
		});
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		getCurrentUserUseCase.stopObserving();
	}
}