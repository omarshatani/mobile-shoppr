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
import com.shoppr.domain.usecase.GetPostByIdUseCase;
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
    private final GetPostByIdUseCase getPostByIdUseCase;
    private final ToggleFavoriteUseCase toggleFavoriteUseCase;

    public final LiveData<User> currentUserProfileLiveData;

    private final MediatorLiveData<List<Post>> _mapPosts = new MediatorLiveData<>();
    public LiveData<List<Post>> mapPosts = _mapPosts;

    private final MediatorLiveData<List<Post>> _bottomSheetPosts = new MediatorLiveData<>();
    public LiveData<List<Post>> bottomSheetPosts = _bottomSheetPosts;

    private final MutableLiveData<Post> _selectedPostDetails = new MutableLiveData<>();
    public LiveData<Post> selectedPostDetails = _selectedPostDetails;

    private final MutableLiveData<Boolean> _isDetailLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isDetailLoading = _isDetailLoading;

    private final MediatorLiveData<Boolean> _isFavorite = new MediatorLiveData<>();

    public LiveData<Boolean> isFavorite() {
        return _isFavorite;
    }

    private final MutableLiveData<Boolean> _locationPermissionGranted = new MutableLiveData<>(false);
    public LiveData<Boolean> locationPermissionGranted = _locationPermissionGranted;

    private final MutableLiveData<Integer> _fabIconResId = new MutableLiveData<>(com.shoppr.core.ui.R.drawable.ic_gps_fixed);
    public LiveData<Integer> fabIconResId = _fabIconResId;

    private final MutableLiveData<Event<LatLng>> _moveToLocationEvent = new MutableLiveData<>();
    public LiveData<Event<LatLng>> moveToLocationEvent = _moveToLocationEvent;

    private final MutableLiveData<Event<Boolean>> _requestPermissionEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> requestPermissionEvent = _requestPermissionEvent;

    private final MutableLiveData<Event<String>> _toastMessageEvent = new MutableLiveData<>();
    public LiveData<Event<String>> toastMessageEvent = _toastMessageEvent;

    private boolean isMapManuallyMoved = false;
    private boolean initialMapCenterAttempted = false;
    private LiveData<List<Post>> currentPostsSource = null;

    @Inject
    public MapViewModel(@NonNull Application application,
                        GetCurrentUserUseCase getCurrentUserUseCase,
                        GetCurrentDeviceLocationUseCase getCurrentDeviceLocationUseCase,
                        UpdateUserDefaultLocationUseCase updateUserDefaultLocationUseCase,
                        GetMapPostsUseCase getMapPostsUseCase,
                        GetPostByIdUseCase getPostByIdUseCase,
                        ToggleFavoriteUseCase toggleFavoriteUseCase) {
        super(application);
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.getCurrentDeviceLocationUseCase = getCurrentDeviceLocationUseCase;
        this.updateUserDefaultLocationUseCase = updateUserDefaultLocationUseCase;
        this.getMapPostsUseCase = getMapPostsUseCase;
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.toggleFavoriteUseCase = toggleFavoriteUseCase;

        this.currentUserProfileLiveData = this.getCurrentUserUseCase.getFullUserProfile();

        _isFavorite.addSource(currentUserProfileLiveData, this::updateFavoriteStatusForSelectedPost);
        _mapPosts.addSource(currentUserProfileLiveData, user -> {
            loadPostsForMap(user != null ? user.getId() : null);
            if (user != null) {
                if (!initialMapCenterAttempted && user.getLatitude() != null && user.getLongitude() != null) {
                    _moveToLocationEvent.postValue(new Event<>(new LatLng(user.getLatitude(), user.getLongitude())));
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
            _bottomSheetPosts.removeSource(currentPostsSource);
        }
        currentPostsSource = getMapPostsUseCase.execute(currentUserId);
        _mapPosts.addSource(currentPostsSource, _mapPosts::setValue);
        _bottomSheetPosts.addSource(currentPostsSource, _bottomSheetPosts::setValue);
    }

    public void onPostMarkerClicked(@NonNull String postId) {
        _isDetailLoading.setValue(true);
        _selectedPostDetails.setValue(null);
        getPostByIdUseCase.execute(postId, new GetPostByIdUseCase.GetPostByIdCallbacks() {
            @Override
            public void onSuccess(@NonNull Post post) {
                _selectedPostDetails.postValue(post);
                updateFavoriteStatusForSelectedPost(currentUserProfileLiveData.getValue());
                _isDetailLoading.postValue(false);
            }

            @Override
            public void onError(@NonNull String message) {
                _toastMessageEvent.postValue(new Event<>("Error loading post details: " + message));
                _isDetailLoading.postValue(false);
            }

            @Override
            public void onNotFound() {
                _toastMessageEvent.postValue(new Event<>("Post not found."));
                _isDetailLoading.postValue(false);
            }
        });
    }

    private void updateFavoriteStatusForSelectedPost(User user) {
        Post selectedPost = _selectedPostDetails.getValue();
        if (selectedPost == null || selectedPost.getId() == null) {
            _isFavorite.setValue(false);
            return;
        }
        if (user != null && user.getFavoritePosts() != null) {
            _isFavorite.setValue(user.getFavoritePosts().contains(selectedPost.getId()));
        } else {
            _isFavorite.setValue(false);
        }
    }

    public void onFavoriteClicked() {
        Post selectedPost = _selectedPostDetails.getValue();
        if (selectedPost == null || selectedPost.getId() == null) return;
        toggleFavoriteUseCase.execute(selectedPost.getId(), new ToggleFavoriteUseCase.FavoriteToggleCallbacks() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(@NonNull String message) {
                _toastMessageEvent.postValue(new Event<>(message));
            }
        });
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
        _fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_location_searching);
    }
    public void onLocationSearching() {
        _fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_location_searching);
    }
    public void onSameLocationClusterClicked(@NonNull List<Post> posts) {
        _bottomSheetPosts.setValue(posts);
        _selectedPostDetails.setValue(null);
    }
    public void clearSelectedPost() {
        _selectedPostDetails.setValue(null);
        _bottomSheetPosts.setValue(_mapPosts.getValue());
    }
    private void fetchAndSaveDeviceLocation(boolean forceMapMove) {
        _fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_location_searching);
        getCurrentDeviceLocationUseCase.execute(new GetCurrentDeviceLocationUseCase.GetDeviceLocationCallbacks() {
            @Override
            public void onDeviceLocationSuccess(@NonNull GetCurrentDeviceLocationUseCase.DeviceLocation deviceLocation) {
                _fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_gps_fixed);
                if (forceMapMove || !isMapManuallyMoved || !initialMapCenterAttempted) {
                    _moveToLocationEvent.postValue(new Event<>(new LatLng(deviceLocation.latitude, deviceLocation.longitude)));
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
                _fabIconResId.setValue(Boolean.TRUE.equals(_locationPermissionGranted.getValue()) ? com.shoppr.core.ui.R.drawable.ic_gps_fixed : com.shoppr.core.ui.R.drawable.ic_location_disabled);
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