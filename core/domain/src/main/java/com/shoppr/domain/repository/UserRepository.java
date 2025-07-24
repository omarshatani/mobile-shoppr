package com.shoppr.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.model.User;

public interface UserRepository {

    // --- No changes to existing interfaces or methods ---
    interface ProfileOperationCallbacks {
        void onSuccess(@NonNull User user);
        void onError(@NonNull String message);
    }
    void getOrCreateUserProfile(
        @NonNull String uid,
        @Nullable String displayName,
        @Nullable String email,
        @Nullable String photoUrl,
        @NonNull ProfileOperationCallbacks callbacks
    );
    interface LocationUpdateCallbacks {
        void onSuccess();
        void onError(@NonNull String message);
    }
    void updateUserDefaultLocation(
        @NonNull String uid,
        double latitude,
        double longitude,
        @Nullable String addressName,
        @NonNull LocationUpdateCallbacks callbacks
    );


    // --- New Interface and Method for Favorites ---

    /**
     * Callbacks for favorite toggle operations.
     */
    interface FavoriteToggleCallbacks {
        void onSuccess(boolean isNowFavorite);

        void onError(@NonNull String message);
    }

    /**
     * Toggles a post's status in the user's favorites list.
     *
     * @param postId              The ID of the post to toggle.
     * @param isCurrentlyFavorite The current favorite status of the post.
     * @param callbacks           The callbacks to be invoked on completion.
     */
    void toggleFavoriteStatus(
        @NonNull String postId,
        boolean isCurrentlyFavorite,
        @NonNull FavoriteToggleCallbacks callbacks
    );
}