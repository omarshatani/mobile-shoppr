package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;

import com.shoppr.model.Post;

public interface GetPostByIdUseCase {
    interface GetPostByIdCallbacks {
        void onSuccess(@NonNull Post post);

        void onError(@NonNull String message);

        void onNotFound();
    }

    /**
     * Executes the logic to fetch a single post by its unique ID.
     *
     * @param postId    The ID of the post to fetch.
     * @param callbacks Callbacks to handle the result.
     */
    void execute(@NonNull String postId, @NonNull GetPostByIdCallbacks callbacks);
}
