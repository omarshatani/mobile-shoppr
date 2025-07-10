package com.shoppr.data.usecase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.shoppr.domain.repository.PostRepository;
import com.shoppr.domain.usecase.GetPostByIdUseCase;
import com.shoppr.model.Post;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GetPostByIdUseCaseImpl implements GetPostByIdUseCase {
    private static final String TAG = "GetPostByIdUseCaseImpl";
    private final PostRepository postRepository;

    @Inject
    public GetPostByIdUseCaseImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public void execute(@NonNull String postId, @NonNull final GetPostByIdCallbacks useCaseCallbacks) {
        Log.d(TAG, "Executing for postId: " + postId);
        postRepository.getPostById(postId, new PostRepository.GetPostByIdCallbacks() {
            @Override
            public void onSuccess(@NonNull Post post) {
                Log.d(TAG, "PostRepository returned success for postId: " + postId);
                useCaseCallbacks.onSuccess(post);
            }

            @Override
            public void onError(@NonNull String message) {
                Log.e(TAG, "PostRepository returned error for postId " + postId + ": " + message);
                useCaseCallbacks.onError(message);
            }

            @Override
            public void onNotFound() {
                Log.w(TAG, "PostRepository reported post not found for postId: " + postId);
                useCaseCallbacks.onNotFound();
            }
        });
    }
}