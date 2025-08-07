package com.shoppr.data.usecase;

import androidx.annotation.NonNull;

import com.shoppr.domain.repository.PostRepository;
import com.shoppr.domain.usecase.GetPostByIdUseCase;
import com.shoppr.model.Post;

import javax.inject.Inject;

public class GetPostByIdUseCaseImpl implements GetPostByIdUseCase {

    private final PostRepository postRepository;

    @Inject
    public GetPostByIdUseCaseImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
		public void execute(@NonNull String postId, @NonNull GetPostByIdCallbacks callbacks) {
			postRepository.getPostById(postId, new PostRepository.PostCallbacks() {
            @Override
            public void onSuccess(@NonNull Post post) {
							callbacks.onSuccess(post);
            }

            @Override
            public void onError(@NonNull String message) {
							callbacks.onError(message);
            }

            @Override
            public void onNotFound() {
							callbacks.onNotFound();
            }
        });
    }
}