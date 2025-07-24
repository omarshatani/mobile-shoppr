package com.shoppr.post;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.repository.AuthenticationRepository;
import com.shoppr.domain.usecase.GetPostByIdUseCase;
import com.shoppr.domain.usecase.ToggleFavoriteUseCase;
import com.shoppr.model.Post;
import com.shoppr.model.User;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PostDetailViewModel extends ViewModel {
    private static final String TAG = "PostDetailViewModel";

    private final GetPostByIdUseCase getPostByIdUseCase;
    private final ToggleFavoriteUseCase toggleFavoriteUseCase;
    private final AuthenticationRepository authenticationRepository;

    private final MutableLiveData<Post> _selectedPost = new MutableLiveData<>();

    public LiveData<Post> getSelectedPost() {
        return _selectedPost;
    }

    // This MediatorLiveData is the key to the fix.
    private final MediatorLiveData<Boolean> _isFavorite = new MediatorLiveData<>();

    public LiveData<Boolean> isFavorite() {
        return _isFavorite;
    }

    private String currentPostId;

    @Inject
    public PostDetailViewModel(
        GetPostByIdUseCase getPostByIdUseCase,
        ToggleFavoriteUseCase toggleFavoriteUseCase,
        AuthenticationRepository authenticationRepository
    ) {
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.toggleFavoriteUseCase = toggleFavoriteUseCase;
        this.authenticationRepository = authenticationRepository;

        // Observe the user's auth state to react to changes in their favorites list
        _isFavorite.addSource(authenticationRepository.getRawAuthState(), this::updateFavoriteStatus);
    }

    public void loadPostDetails(String postId) {
        this.currentPostId = postId;
        getPostByIdUseCase.execute(postId, new GetPostByIdUseCase.GetPostByIdCallbacks() {
            @Override
            public void onSuccess(@NonNull Post post) {
                _selectedPost.setValue(post);
                // Now that we have a post, check its favorite status against the current user
                updateFavoriteStatus(authenticationRepository.getRawAuthState().getValue());
            }

            @Override
            public void onError(@NonNull String message) {
            }
            @Override
            public void onNotFound() {
            }
        });
    }

    private void updateFavoriteStatus(User user) {
        if (currentPostId == null) {
            return; // Don't do anything if we don't have a post yet
        }
        if (user != null && user.getFavoritePosts() != null) {
            _isFavorite.setValue(user.getFavoritePosts().contains(currentPostId));
        } else {
            _isFavorite.setValue(false);
        }
    }

    public void toggleFavorite() {
        if (currentPostId == null || !authenticationRepository.isUserLoggedIn()) {
            return;
        }

        boolean currentlyFavorite = _isFavorite.getValue() != null && _isFavorite.getValue();
        toggleFavoriteUseCase.execute(currentPostId, currentlyFavorite, new ToggleFavoriteUseCase.FavoriteToggleCallbacks() {
            @Override
            public void onSuccess(boolean isNowFavorite) {
                // The LiveData will update automatically from the user auth state listener.
            }

            @Override
            public void onError(@NonNull String message) {
                // Optionally show an error
                Log.e(TAG, "Error toggling favorite: " + message);
            }
        });
    }
}