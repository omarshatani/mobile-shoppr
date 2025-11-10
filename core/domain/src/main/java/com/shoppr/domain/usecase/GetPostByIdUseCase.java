package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;

import com.shoppr.model.Post;

public interface GetPostByIdUseCase {
	interface GetPostByIdCallbacks {
		void onSuccess(@NonNull Post post);

		void onError(@NonNull String message);

		void onNotFound();
	}

	void execute(@NonNull String postId, @NonNull GetPostByIdCallbacks callbacks);
}