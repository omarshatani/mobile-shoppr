package com.shoppr.data.usecase;

import androidx.lifecycle.LiveData;

import com.shoppr.domain.repository.PostRepository;
import com.shoppr.domain.usecase.GetMyPostsUseCase;
import com.shoppr.model.Post;

import java.util.List;

import javax.inject.Inject;

public class GetMyPostsUseCaseImpl implements GetMyPostsUseCase {

    private final PostRepository postRepository;

    @Inject
    public GetMyPostsUseCaseImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
		public LiveData<List<Post>> execute(String userId) {
			return postRepository.getPostsForUser(userId);
    }
}