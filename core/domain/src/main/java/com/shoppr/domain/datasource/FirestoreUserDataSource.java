package com.shoppr.domain.datasource;

import androidx.annotation.NonNull;

import com.shoppr.model.User;

import java.util.List;

public interface FirestoreUserDataSource {

    // --- No changes to existing interfaces or methods ---
    interface FirestoreOperationCallbacks {
        void onSuccess(@NonNull User user);
        void onError(@NonNull String message);
        void onNotFound();
    }
    void getUser(@NonNull String uid, @NonNull FirestoreOperationCallbacks callbacks);

    void createUser(@NonNull User user, @NonNull FirestoreOperationCallbacks callbacks);

    void updateUser(@NonNull User user, @NonNull FirestoreOperationCallbacks callbacks);


    // --- New Interface and Method for Favorites ---

    /**
     * Callbacks for favorite add/remove operations.
     */
    interface FavoriteUpdateCallbacks {
        void onSuccess(@NonNull List<String> updatedFavorites);

        void onError(@NonNull String message);
    }

    /**
     * Atomically adds or removes a postId from the user's favoritePosts list.
     *
     * @param uid       The ID of the user.
     * @param postId    The ID of the post to add or remove.
     * @param shouldAdd True to add to favorites, false to remove.
     * @param callbacks The callbacks to be invoked on completion.
     */
    void updateUserFavorites(
        @NonNull String uid,
        @NonNull String postId,
        boolean shouldAdd,
        @NonNull FavoriteUpdateCallbacks callbacks
    );
}