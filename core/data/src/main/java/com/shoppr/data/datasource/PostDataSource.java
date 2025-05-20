package com.shoppr.data.datasource;

import androidx.annotation.NonNull;

import com.shoppr.model.Post;

public interface PostDataSource {
    interface FirestorePostCallbacks {
        void onSuccess();

        void onError(@NonNull String message);
    }

    interface FirestoreGetPostCallbacks {
        void onSuccess(@NonNull Post post);

        void onError(@NonNull String message);
    }

    /**
     * Saves a post to Firestore.
     *
     * @param post      The Post object to save.
     * @param callbacks Callbacks for the operation.
     */
    void savePost(@NonNull Post post, @NonNull FirestorePostCallbacks callbacks);

    /**
     * Retrieves a post from Firestore by its ID.
     *
     * @param postId    The ID of the post to retrieve.
     * @param callbacks Callbacks for the operation.
     */
    void getPostById(@NonNull String postId, @NonNull FirestoreGetPostCallbacks callbacks);


    /**
     * Updates an existing post in Firestore.
     *
     * @param post The Post object with updated data.
     */
    void updatePost(@NonNull Post post);

    /**
     * Deletes a post from Firestore by its ID.
     *
     * @param postId The ID of the post to delete.
     */
    void deletePost(@NonNull String postId);

}
