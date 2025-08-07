package com.shoppr.data.usecase;

import androidx.lifecycle.LiveData;

import com.shoppr.domain.repository.PostRepository;
import com.shoppr.domain.usecase.GetMapPostsUseCase;
import com.shoppr.model.Post;

import java.util.List;

import javax.inject.Inject;

public class GetMapPostsUseCaseImpl implements GetMapPostsUseCase {

	private final PostRepository postRepository;

	@Inject
	public GetMapPostsUseCaseImpl(PostRepository postRepository) {
		this.postRepository = postRepository;
	}

	@Override
	public LiveData<List<Post>> execute(String currentUserIdToExclude) {
		return postRepository.getFeedPosts(currentUserIdToExclude);
	}
}