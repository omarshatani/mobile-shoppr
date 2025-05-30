package com.shoppr.post;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetMyPostsUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.Post;
import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PostFragmentViewModel extends ViewModel { // Changed to ViewModel unless Application context is needed
	private static final String TAG = "PostFragmentViewModel";

	private final GetCurrentUserUseCase getCurrentUserUseCase;
	private final GetMyPostsUseCase getMyPostsUseCase;

	public final LiveData<User> currentUserProfileLiveData;
	// public final LiveData<Event<String>> currentUserProfileErrorEvents; // If GetCurrentUserUseCase exposes errors

	private final MediatorLiveData<List<Post>> _posts = new MediatorLiveData<>();
	public LiveData<List<Post>> posts = _posts;

	private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
	public LiveData<Boolean> isLoading = _isLoading;

	private final MutableLiveData<Event<String>> _errorMessage = new MutableLiveData<>();
	public LiveData<Event<String>> errorMessage = _errorMessage;

	private final MutableLiveData<Event<NavigationRoute>> _navigationCommand = new MutableLiveData<>();
	public LiveData<Event<NavigationRoute>> getNavigationCommand() {
		return _navigationCommand;
	}

	private LiveData<List<Post>> currentPostsSource = null;

	@Inject
	public PostFragmentViewModel(
			GetCurrentUserUseCase getCurrentUserUseCase,
			GetMyPostsUseCase getMyPostsUseCase) {
		this.getCurrentUserUseCase = getCurrentUserUseCase;
		this.getMyPostsUseCase = getMyPostsUseCase;

		this.currentUserProfileLiveData = this.getCurrentUserUseCase.getFullUserProfile();
		// this.currentUserProfileErrorEvents = this.getCurrentUserUseCase.getProfileErrorEvents();

		// Observe the current user. When the user is available, fetch their posts.
		_posts.addSource(this.currentUserProfileLiveData, user -> {
			if (user != null && user.getId() != null) {
				Log.d(TAG, "Current user available: " + user.getId() + ". Fetching their posts.");
				fetchUserPosts(user.getId());
			} else {
				Log.d(TAG, "No current user or user ID is null. Clearing posts.");
				_posts.setValue(new ArrayList<>()); // Clear posts if no user
				// Optionally, post an error or specific message if user is null after being logged in.
				// if (user == null && wasPreviouslyLoggedIn) {
				//    _errorMessage.postValue(new Event<>("Please log in to see your posts."));
				// }
			}
		});

		// Observe errors from GetCurrentUserUseCase if it exposes them
		// _posts.addSource(this.currentUserProfileErrorEvents, errorEvent -> { ... });
	}

	private void fetchUserPosts(@NonNull String userId) {
		Log.d(TAG, "fetchUserPosts called for user ID: " + userId);
		_isLoading.setValue(true);

		if (currentPostsSource != null) {
			_posts.removeSource(currentPostsSource);
		}
		currentPostsSource = getMyPostsUseCase.execute(userId); // This should return LiveData<List<Post>>

		_posts.addSource(currentPostsSource, postList -> {
			_isLoading.setValue(false);
			if (postList != null) {
				Log.d(TAG, "User's posts LiveData updated. Count: " + postList.size());
				_posts.setValue(postList);
			} else {
				Log.w(TAG, "Received null posts list from use case for user: " + userId);
				_posts.setValue(new ArrayList<>()); // Set to empty list if null
				_errorMessage.postValue(new Event<>("Could not load your posts."));
			}
		});
	}

	public void refreshPosts() {
		User currentUser = currentUserProfileLiveData.getValue();
		if (currentUser != null && currentUser.getId() != null) {
			Log.d(TAG, "Refreshing posts for user: " + currentUser.getId());
			fetchUserPosts(currentUser.getId());
		} else {
			Log.d(TAG, "Cannot refresh posts, no current user.");
			_posts.setValue(new ArrayList<>());
		}
	}

	public void navigateToCreatePost() {
		_navigationCommand.setValue(new Event<>(new NavigationRoute.PostsToCreatePost()));
	}

	// Called from Fragment's onStart/onStop
	public void startObservingUser() {
		Log.d(TAG, "ViewModel: Starting to observe current user.");
		getCurrentUserUseCase.startObserving();
	}

	public void stopObservingUser() {
		Log.d(TAG, "ViewModel: Stopping user observation.");
		getCurrentUserUseCase.stopObserving();
		if (currentPostsSource != null) {
			_posts.removeSource(currentPostsSource);
			currentPostsSource = null;
		}
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		Log.d(TAG, "PostFragmentViewModel onCleared.");
		stopObservingUser(); // Ensure cleanup
	}
}
