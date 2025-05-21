package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;

import com.shoppr.model.Post;

public interface SavePostUseCase {
    interface SavePostCallbacks {
        void onSaveSuccess();

        void onSaveError(@NonNull String message);
    }

    /**
     * Executes the post saving logic.
     *
     * @param post      The Post object to be saved.
     * @param callbacks Callbacks to signal completion.
     */
    void execute(@NonNull Post post, @NonNull SavePostCallbacks callbacks);
}