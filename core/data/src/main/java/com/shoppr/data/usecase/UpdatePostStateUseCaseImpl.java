package com.shoppr.data.usecase;

import androidx.annotation.NonNull;

import com.shoppr.domain.repository.PostRepository;
import com.shoppr.domain.usecase.UpdatePostStateUseCase;
import com.shoppr.model.ListingState;

import javax.inject.Inject;

public class UpdatePostStateUseCaseImpl implements UpdatePostStateUseCase {

	private final PostRepository postRepository;

	@Inject
	public UpdatePostStateUseCaseImpl(PostRepository postRepository) {
		this.postRepository = postRepository;
	}

	@Override
	public void execute(@NonNull String postId, @NonNull ListingState newListingState, @NonNull UpdatePostStateCallbacks callbacks) {
		postRepository.updatePostState(postId, newListingState, new PostRepository.UpdatePostStateCallbacks() {
			@Override
			public void onSuccess() {
				callbacks.onSuccess();
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}
}