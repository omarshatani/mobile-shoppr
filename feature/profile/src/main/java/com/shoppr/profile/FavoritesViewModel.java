package com.shoppr.profile; // Or your preferred package

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetFavoritePostsUseCase;
import com.shoppr.domain.usecase.ToggleFavoriteUseCase;
import com.shoppr.model.Post;
import com.shoppr.model.User;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FavoritesViewModel extends ViewModel {

	private final GetCurrentUserUseCase getCurrentUserUseCase;
	private final ToggleFavoriteUseCase toggleFavoriteUseCase;
	private final GetFavoritePostsUseCase getFavoritePostsUseCase;

	private final LiveData<List<Post>> favoritePosts;

	@Inject
	public FavoritesViewModel(
			GetCurrentUserUseCase getCurrentUserUseCase,
			ToggleFavoriteUseCase toggleFavoriteUseCase,
			GetFavoritePostsUseCase getFavoritePostsUseCase
	) {
		this.getCurrentUserUseCase = getCurrentUserUseCase;
		this.toggleFavoriteUseCase = toggleFavoriteUseCase;
		this.getFavoritePostsUseCase = getFavoritePostsUseCase;

		// The LiveData is initialized by executing the use case.
		// It will automatically update when the user's favorites change.
		this.favoritePosts = this.getFavoritePostsUseCase.execute();
	}

	public void onFragmentStarted() {
		// This is important to ensure the underlying user profile is being observed
		getCurrentUserUseCase.startObserving();
	}

	public void onFragmentStopped() {
		getCurrentUserUseCase.stopObserving();
	}

	/**
	 * Exposes the LiveData of the current user, which the fragment will observe
	 * to update the favorite status in the adapter.
	 */
	public LiveData<User> getCurrentUser() {
		return getCurrentUserUseCase.getFullUserProfile();
	}

	/**
	 * Exposes the LiveData containing the list of full Post objects that are favorites.
	 */
	public LiveData<List<Post>> getFavoritePosts() {
		return favoritePosts;
	}

	/**
	 * Unfavorites a post. On this screen, the post is always a favorite,
	 * so isCurrentlyFavorite is always true.
	 *
	 * @param post The post to unfavorite.
	 */
	public void unfavoritePost(Post post) {
		if (post == null || post.getId() == null) return;

		toggleFavoriteUseCase.execute(post.getId(), true, new ToggleFavoriteUseCase.FavoriteToggleCallbacks() {
			@Override
			public void onSuccess(boolean isNowFavorite) {
				// The UI will update automatically because the LiveData from the
				// GetCurrentUserUseCase will fire, which in turn triggers the
				// GetFavoritePostsUseCase to refetch the list.
			}

			@Override
			public void onError(String message) {
				// Optionally, you can expose an error LiveData to the fragment
				// to show a Toast or a Snackbar.
			}
		});
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		// Ensure we stop observing when the ViewModel is destroyed
		getCurrentUserUseCase.stopObserving();
	}
}