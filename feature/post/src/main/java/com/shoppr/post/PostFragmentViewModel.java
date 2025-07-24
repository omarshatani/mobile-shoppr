package com.shoppr.post;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetMyPostsUseCase;
import com.shoppr.domain.usecase.ToggleFavoriteUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.Post;
import com.shoppr.model.User;
import com.shoppr.navigation.NavigationRoute;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PostFragmentViewModel extends ViewModel {
	private static final String TAG = "PostFragmentViewModel";

	private final GetCurrentUserUseCase getCurrentUserUseCase;
	private final GetMyPostsUseCase getMyPostsUseCase;
	private final ToggleFavoriteUseCase toggleFavoriteUseCase;

	public final LiveData<User> currentUserProfileLiveData;

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
			GetMyPostsUseCase getMyPostsUseCase,
			ToggleFavoriteUseCase toggleFavoriteUseCase) {
		this.getCurrentUserUseCase = getCurrentUserUseCase;
		this.getMyPostsUseCase = getMyPostsUseCase;
		this.toggleFavoriteUseCase = toggleFavoriteUseCase;

		this.currentUserProfileLiveData = this.getCurrentUserUseCase.getFullUserProfile();

		_posts.addSource(this.currentUserProfileLiveData, user -> {
			if (user != null && user.getId() != null) {
				fetchUserPosts(user.getId());
			} else {
				_posts.setValue(new ArrayList<>());
			}
		});
	}

	public void onFavoriteClicked(@NonNull Post post) {
		User currentUser = currentUserProfileLiveData.getValue();
		if (currentUser == null || post.getId() == null) {
			_errorMessage.postValue(new Event<>("You must be logged in to add favorites."));
			return;
		}

		List<String> favorites = currentUser.getFavoritePosts();
		boolean isCurrentlyFavorite = favorites != null && favorites.contains(post.getId());

		toggleFavoriteUseCase.execute(post.getId(), isCurrentlyFavorite, new ToggleFavoriteUseCase.FavoriteToggleCallbacks() {
			@Override
			public void onSuccess(boolean isNowFavorite) {
				Log.d(TAG, "Favorite status toggled successfully for post: " + post.getId());
			}

			@Override
			public void onError(@NonNull String message) {
				_errorMessage.postValue(new Event<>(message));
			}
		});
	}

	// --- All your other existing methods remain unchanged ---
	private void fetchUserPosts(@NonNull String userId) {
		_isLoading.setValue(true);
		if (currentPostsSource != null) {
			_posts.removeSource(currentPostsSource);
		}
		currentPostsSource = getMyPostsUseCase.execute(userId);
		_posts.addSource(currentPostsSource, postList -> {
			_isLoading.setValue(false);
			_posts.setValue(postList != null ? postList : new ArrayList<>());
		});
	}

	public void refreshPosts() {
		User currentUser = currentUserProfileLiveData.getValue();
		if (currentUser != null && currentUser.getId() != null) {
			fetchUserPosts(currentUser.getId());
		} else {
			_posts.setValue(new ArrayList<>());
		}
	}

	public void navigateToCreatePost() {
		_navigationCommand.setValue(new Event<>(new NavigationRoute.PostsToCreatePost()));
	}

	public void startObservingUser() {
		getCurrentUserUseCase.startObserving();
	}

	public void stopObservingUser() {
		getCurrentUserUseCase.stopObserving();
		if (currentPostsSource != null) {
			_posts.removeSource(currentPostsSource);
			currentPostsSource = null;
		}
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		stopObservingUser();
	}
}