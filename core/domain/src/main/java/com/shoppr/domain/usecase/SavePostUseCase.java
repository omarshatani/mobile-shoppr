package com.shoppr.domain.usecase;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.shoppr.model.Post;

import java.util.List;

public interface SavePostUseCase {
    interface SavePostCallback {
        void onSuccess(@NonNull Post createdPost);

        void onError(String message);
    }

    void execute(Post post, List<Uri> imageUris, SavePostCallback callback);
}