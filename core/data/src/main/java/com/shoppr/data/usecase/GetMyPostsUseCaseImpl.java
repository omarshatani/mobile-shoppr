package com.shoppr.data.usecase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.shoppr.domain.repository.PostRepository;
import com.shoppr.domain.usecase.GetMyPostsUseCase;
import com.shoppr.model.Post;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GetMyPostsUseCaseImpl implements GetMyPostsUseCase {
    private static final String TAG = "GetMyPostsUseCaseImpl";
    private final PostRepository postRepository;

    @Inject
    public GetMyPostsUseCaseImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public LiveData<List<Post>> execute(@NonNull String userId) {
        Log.d(TAG, "Executing GetMyPostsUseCase for user: " + userId);
        // Delegate directly to the repository method
        return postRepository.getPostsCreatedByUser(userId);
    }
}
