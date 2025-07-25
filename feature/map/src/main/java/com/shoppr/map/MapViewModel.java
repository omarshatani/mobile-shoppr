package com.shoppr.map;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.model.LatLng;
import com.shoppr.domain.usecase.GetCurrentDeviceLocationUseCase;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetMapPostsUseCase;
import com.shoppr.domain.usecase.GetPostByIdUseCase;
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


    // LiveData for the currently authenticated user (with full profile)
    public final LiveData<User> currentUserProfileLiveData;

    // LiveData for posts to be displayed on the map
    // Using MediatorLiveData to combine results from GetMapPostsUseCase based on current user
    private final MediatorLiveData<List<Post>> _mapPosts = new MediatorLiveData<>();
    public LiveData<List<Post>> mapPosts = _mapPosts;

    private final MediatorLiveData<List<Post>> _bottomSheetPosts = new MediatorLiveData<>();
    public LiveData<List<Post>> bottomSheetPosts = _bottomSheetPosts;

    // For the specific post selected by the user
    private final MutableLiveData<Post> _selectedPostDetails = new MutableLiveData<>();
    public LiveData<Post> selectedPostDetails = _selectedPostDetails;

    // To indicate loading of a single post's details
    private final MutableLiveData<Boolean> _isDetailLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isDetailLoading = _isDetailLoading;


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
    private boolean initialMapCenterAttempted = false;
    private final Observer<User> userProfileAndPostsObserver;
    private LiveData<List<Post>> currentPostsSource = null;
    private final LiveData<List<Post>> currentMapPostsSource = null;

    @Inject
    public MapViewModel(@NonNull Application application,
                        GetCurrentUserUseCase getCurrentUserUseCase,
                        GetCurrentDeviceLocationUseCase getCurrentDeviceLocationUseCase,
                        UpdateUserDefaultLocationUseCase updateUserDefaultLocationUseCase,
                        GetMapPostsUseCase getMapPostsUseCase, GetPostByIdUseCase getPostByIdUseCase) { // Inject GetMapPostsUseCase
        super(application);
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.getCurrentDeviceLocationUseCase = getCurrentDeviceLocationUseCase;
        this.updateUserDefaultLocationUseCase = updateUserDefaultLocationUseCase;
        this.getMapPostsUseCase = getMapPostsUseCase;
        this.getPostByIdUseCase = getPostByIdUseCase;

        this.currentUserProfileLiveData = this.getCurrentUserUseCase.getFullUserProfile();

        userProfileAndPostsObserver = user -> {
            String currentUserId = null;
            if (user != null) {
                currentUserId = user.getId();
                Log.d(TAG, "UserProfileObserver: User data received - " + user.getName() + " (UID: " + currentUserId + ")");

                if (!initialMapCenterAttempted && user.getLatitude() != null && user.getLongitude() != null) {
                    Log.d(TAG, "UserProfileObserver: User has last known location. Centering map: " +
                        user.getLatitude() + ", " + user.getLongitude());
                    _moveToLocationEvent.postValue(new Event<>(new LatLng(user.getLatitude(), user.getLongitude())));
                    initialMapCenterAttempted = true;
                } else if (!initialMapCenterAttempted) {
                    Log.d(TAG, "UserProfileObserver: User profile loaded, but no last known location saved, or already attempted center. Will attempt to fetch current device location if permission is granted.");
                    if (Boolean.TRUE.equals(_locationPermissionGranted.getValue())) {
                        fetchAndSaveDeviceLocation(true); // forceMapMove true for initial centering attempt
                    }
                    initialMapCenterAttempted = true; // Mark attempt even if going for device location
                }
            } else {
                Log.d(TAG, "UserProfileObserver: User is null (logged out or profile error).");
                initialMapCenterAttempted = false; // Reset for next login session
            }
            // Fetch/refresh posts for the map, potentially excluding the current user's posts
            loadPostsForMap(currentUserId);
        };

    }

    private void loadPostsForMap(@Nullable String currentUserId) {
        if (currentPostsSource != null) {
            _mapPosts.removeSource(currentPostsSource);
            _bottomSheetPosts.removeSource(currentPostsSource); // Also remove from bottom sheet source
        }
        currentPostsSource = getMapPostsUseCase.execute(currentUserId);
        // Both LiveData objects observe the same source initially
        _mapPosts.addSource(currentPostsSource, _mapPosts::setValue);
        _bottomSheetPosts.addSource(currentPostsSource, _bottomSheetPosts::setValue);
    }


    public void onMapFragmentStarted() {
        Log.d(TAG, "MapFragment started. Starting user profile and posts observation.");
        getCurrentUserUseCase.startObserving(); // Start observing the user's full profile
        currentUserProfileLiveData.observeForever(userProfileAndPostsObserver);
        // Initial call to load posts (will be re-triggered by userProfileObserver if user logs in/out,
        // or if the LiveData from getMapPostsUseCase emits again)
        User user = currentUserProfileLiveData.getValue();
        loadPostsForMap(user != null ? user.getId() : null);
    }

    public void onMapFragmentStopped() {
        currentUserProfileLiveData.removeObserver(userProfileAndPostsObserver);
        getCurrentUserUseCase.stopObserving();
        if (currentPostsSource != null) {
            _mapPosts.removeSource(currentPostsSource);
            _bottomSheetPosts.removeSource(currentPostsSource);
        }
    }

    public void onLocationPermissionResult(boolean isGranted) {
        Log.d(TAG, "Location permission result: " + isGranted);
        _locationPermissionGranted.setValue(isGranted);
        if (isGranted) {
            _fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_gps_fixed);
            // If initial centering hasn't happened and user grants permission, try to center.
            boolean shouldForceMove = !initialMapCenterAttempted;
            fetchAndSaveDeviceLocation(shouldForceMove);
        } else {
            _fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_location_disabled);
        }
    }

    public void onMyLocationButtonClicked() {
        Log.d(TAG, "My Location FAB clicked.");
        if (Boolean.TRUE.equals(_locationPermissionGranted.getValue())) {
            Log.d(TAG, "Permission granted, fetching device location.");
            isMapManuallyMoved = false;
            initialMapCenterAttempted = false; // Allow re-centering attempt
            fetchAndSaveDeviceLocation(true); // Force map move
        } else {
            Log.d(TAG, "Permission not granted, requesting permission.");
            _requestPermissionEvent.setValue(new Event<>(true));
        }
    }

    public void onMapManualMoveStarted() {
        Log.d(TAG, "Map manually moved by user.");
        isMapManuallyMoved = true;
        _fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_location_searching);
    }

    public void onLocationSearching() {
        Log.d(TAG, "onLocationSearching: Updating FAB to searching icon.");
        _fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_location_searching);
    }

    /**
     * Called from the MapFragment when a post marker is clicked.
     *
     * @param postId The ID of the post associated with the clicked marker.
     */
    public void onPostMarkerClicked(@NonNull String postId) {
        if (postId.isEmpty()) {
            Log.w(TAG, "onPostMarkerClicked called with empty postId.");
            return;
        }
        Log.d(TAG, "Fetching details for post ID: " + postId);
        _isDetailLoading.setValue(true);
        _selectedPostDetails.setValue(null);

        getPostByIdUseCase.execute(postId, new GetPostByIdUseCase.GetPostByIdCallbacks() {
            @Override
            public void onSuccess(@NonNull Post post) {
                Log.d(TAG, "Successfully fetched details for post: " + post.getTitle());
                _selectedPostDetails.postValue(post);
                _isDetailLoading.postValue(false);
            }

            @Override
            public void onError(@NonNull String message) {
                Log.e(TAG, "Error fetching post details for ID " + postId + ": " + message);
                _toastMessageEvent.postValue(new Event<>("Error loading post details: " + message));
                _isDetailLoading.postValue(false);
            }

            @Override
            public void onNotFound() {
                Log.w(TAG, "Post with ID " + postId + " not found.");
                _toastMessageEvent.postValue(new Event<>("Post not found. It may have been deleted."));
                _isDetailLoading.postValue(false);
            }
        });
    }

    public void onSameLocationClusterClicked(@NonNull List<Post> posts) {
        Log.d(TAG, "Same location cluster clicked. Updating bottom sheet list only.");
        _bottomSheetPosts.setValue(posts); // Update only the bottom sheet's data source
        _selectedPostDetails.setValue(null); // Ensure detail view is hidden
    }


    /**
     * Called when the user dismisses the post detail view (e.g., collapses the bottom sheet back to the list).
     */
    public void clearSelectedPost() {
        _selectedPostDetails.setValue(null);
        // Reset the bottom sheet list to the main list of all map posts
        _bottomSheetPosts.setValue(_mapPosts.getValue());
    }
    private void fetchAndSaveDeviceLocation(boolean forceMapMove) {
        User currentUser = currentUserProfileLiveData.getValue();
        if (currentUser == null || currentUser.getId() == null) {
            Log.w(TAG, "Cannot fetch and save device location: current user or UID is null.");
            _fabIconResId.setValue(Boolean.TRUE.equals(_locationPermissionGranted.getValue()) ? com.shoppr.core.ui.R.drawable.ic_gps_fixed : com.shoppr.core.ui.R.drawable.ic_location_disabled);
            return;
        }
        final String currentUserId = currentUser.getId();

        Log.d(TAG, "Attempting to fetch device location for user: " + currentUserId);
        // FAB icon is already set to searching if called from onMyLocationButtonClicked or onLocationSearching

        getCurrentDeviceLocationUseCase.execute(new GetCurrentDeviceLocationUseCase.GetDeviceLocationCallbacks() {
            @Override
            public void onDeviceLocationSuccess(@NonNull GetCurrentDeviceLocationUseCase.DeviceLocation deviceLocation) {
                Log.i(TAG, "Device location fetched: " + deviceLocation.latitude + "," + deviceLocation.longitude);
                _fabIconResId.setValue(com.shoppr.core.ui.R.drawable.ic_gps_fixed);

                if (forceMapMove || !isMapManuallyMoved || !initialMapCenterAttempted) {
                    _moveToLocationEvent.postValue(new Event<>(new LatLng(deviceLocation.latitude, deviceLocation.longitude)));
                    initialMapCenterAttempted = true;
                    isMapManuallyMoved = false;
                }

                updateUserDefaultLocationUseCase.execute(
                        currentUserId,
                        deviceLocation.latitude,
                        deviceLocation.longitude,
                        deviceLocation.address,
                        new UpdateUserDefaultLocationUseCase.UpdateLocationCallbacks() {
                            @Override
                            public void onLocationUpdateSuccess() {
                                Log.i(TAG, "User default location updated in Firestore for user: " + currentUserId);
                            }

                            @Override
                            public void onLocationUpdateError(@NonNull String message) {
                                Log.e(TAG, "Failed to update user default location in Firestore: " + message);
                                _toastMessageEvent.postValue(new Event<>("Could not save current location."));
                            }
                        }
                );
            }

            @Override
            public void onDeviceLocationError(@NonNull String message) {
                Log.e(TAG, "Failed to fetch device location: " + message);
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
        Log.d(TAG, "MapViewModel onCleared. Removing observers.");
        currentUserProfileLiveData.removeObserver(userProfileAndPostsObserver);
        getCurrentUserUseCase.stopObserving();
    }
}
