package com.shoppr.post;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.GetPostByIdUseCase;
import com.shoppr.model.Post;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PostDetailViewModel extends ViewModel {

    private final GetPostByIdUseCase getPostByIdUseCase;
    private final MutableLiveData<Post> selectedPost = new MutableLiveData<>();

    @Inject
    public PostDetailViewModel(GetPostByIdUseCase getPostByIdUseCase) {
        this.getPostByIdUseCase = getPostByIdUseCase;
    }

    public LiveData<Post> getSelectedPost() {
        return selectedPost;
    }

    public void loadPostDetails(String postId) {
        getPostByIdUseCase.execute(postId, new GetPostByIdUseCase.GetPostByIdCallbacks() {
            @Override
            public void onSuccess(@NonNull Post post) {
                selectedPost.setValue(post);
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
}