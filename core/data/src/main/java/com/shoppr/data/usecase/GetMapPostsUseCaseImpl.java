package com.shoppr.data.usecase;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.shoppr.domain.repository.PostRepository;
import com.shoppr.domain.usecase.GetMapPostsUseCase;
import com.shoppr.model.Post;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GetMapPostsUseCaseImpl implements GetMapPostsUseCase {
	private static final String TAG = "GetMapPostsUseCaseImpl";
	private final PostRepository postRepository;

	@Inject
	public GetMapPostsUseCaseImpl(PostRepository postRepository) {
		this.postRepository = postRepository;
	}

	@Override
	public LiveData<List<Post>> execute(@Nullable String currentUserId) {
		Log.d(TAG, "Executing GetMapPostsUseCase. Excluding user: " + currentUserId);
		return postRepository.getPostsForMap(currentUserId);
	}
}
