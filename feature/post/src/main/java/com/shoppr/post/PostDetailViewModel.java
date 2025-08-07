package com.shoppr.post;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetPostByIdUseCase;
import com.shoppr.domain.usecase.ToggleFavoriteUseCase;
import com.shoppr.model.Post;
import com.shoppr.model.User;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PostDetailViewModel extends ViewModel {

    private final GetPostByIdUseCase getPostByIdUseCase;
    private final ToggleFavoriteUseCase toggleFavoriteUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    private final MutableLiveData<Post> _selectedPost = new MutableLiveData<>();
    public LiveData<Post> getSelectedPost() {
        return _selectedPost;
    }

    private final MediatorLiveData<Boolean> _isFavorite = new MediatorLiveData<>();
    public LiveData<Boolean> isFavorite() {
        return _isFavorite;
    }

    private String currentPostId;

    @Inject
    public PostDetailViewModel(
        GetPostByIdUseCase getPostByIdUseCase,
        ToggleFavoriteUseCase toggleFavoriteUseCase,
        GetCurrentUserUseCase getCurrentUserUseCase
    ) {
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.toggleFavoriteUseCase = toggleFavoriteUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;

			// The Mediator now observes the single source of truth for the user's profile.
        _isFavorite.addSource(getCurrentUserUseCase.getFullUserProfile(), this::updateFavoriteStatus);
    }

	public void onFragmentStarted() {
        getCurrentUserUseCase.startObserving();
    }

	public void onFragmentStopped() {
        getCurrentUserUseCase.stopObserving();
    }

    public void loadPostDetails(String postId) {
        this.currentPostId = postId;
        getPostByIdUseCase.execute(postId, new GetPostByIdUseCase.GetPostByIdCallbacks() {
            @Override
            public void onSuccess(@NonNull Post post) {
                _selectedPost.setValue(post);
							// After the post is loaded, check the favorite status against the current user.
                updateFavoriteStatus(getCurrentUserUseCase.getFullUserProfile().getValue());
            }

            @Override
            public void onError(@NonNull String message) {
							// Handle error
            }

            @Override
            public void onNotFound() {
							// Handle not found
            }
        });
    }

    private void updateFavoriteStatus(User user) {
        if (currentPostId == null) {
            _isFavorite.setValue(false);
            return;
        }

        if (user != null && user.getFavoritePosts() != null) {
					boolean isFavorited = user.getFavoritePosts().contains(currentPostId);
					_isFavorite.setValue(isFavorited);
        } else {
            _isFavorite.setValue(false);
        }
    }

    public void toggleFavorite() {
			if (currentPostId == null) return;

			toggleFavoriteUseCase.execute(currentPostId, new ToggleFavoriteUseCase.FavoriteToggleCallbacks() {
            @Override
						public void onSuccess() {
							// No action needed. The UI will update automatically because the
							// getFullUserProfile() LiveData will emit a new value.
            }

				@Override
            public void onError(@NonNull String message) {
					// Optionally show an error message.
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        getCurrentUserUseCase.stopObserving();
    }
}