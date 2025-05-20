package com.shoppr.domain;

import android.util.Log;

import androidx.annotation.NonNull;

import com.shoppr.data.repository.PostRepository;
import com.shoppr.model.Post;

import javax.inject.Inject;

public class SavePostUseCaseImpl implements SavePostUseCase {
	private static final String TAG = "SavePostUseCaseImpl";
	private final PostRepository postRepository;

	@Inject
	public SavePostUseCaseImpl(PostRepository postRepository) {
		this.postRepository = postRepository;
	}

	@Override
	public void execute(@NonNull Post post, @NonNull final SavePostCallbacks callbacks) {
		Log.d(TAG, "Executing SavePostUseCase for post: " + post.getTitle());
		// Add any business logic here before saving if needed
		// e.g., validation, adding timestamps if not handled by repository/datasource

		postRepository.savePost(post, new PostRepository.SavePostCallbacks() {
			@Override
			public void onSaveSuccess() {
				Log.d(TAG, "PostRepository reported save success.");
				callbacks.onSaveSuccess();
			}

			@Override
			public void onSaveError(@NonNull String message) {
				Log.e(TAG, "PostRepository reported save error: " + message);
				callbacks.onSaveError(message);
			}
		});
	}
}