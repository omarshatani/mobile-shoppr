package com.shoppr.data.usecase;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.shoppr.domain.repository.PostRepository;
import com.shoppr.domain.usecase.SavePostUseCase;
import com.shoppr.model.Post;

import java.util.List;

import javax.inject.Inject;

public class SavePostUseCaseImpl implements SavePostUseCase {
	private final PostRepository postRepository;

	@Inject
	public SavePostUseCaseImpl(PostRepository postRepository) {
		this.postRepository = postRepository;
	}

	@Override
	public void execute(Post post, List<Uri> imageUris, SavePostCallback callback) {
		postRepository.createPost(post, imageUris, new PostRepository.PostCreationCallbacks() {
			@Override
			public void onSuccess(@NonNull Post createdPost) {
				callback.onSuccess(createdPost);
			}

			@Override
			public void onError(String message) {
				callback.onError(message);
			}
		});
	}
}