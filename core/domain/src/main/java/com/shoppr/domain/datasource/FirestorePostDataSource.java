package com.shoppr.domain.datasource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.shoppr.model.Post;

import java.util.List;

public interface FirestorePostDataSource {
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
		 * Callbacks for retrieving a single post by its ID from Firestore.
		 */
		interface FirestoreGetPostByIdCallbacks {
        void onSuccess(@NonNull Post post);
        void onError(@NonNull String message);
        void onNotFound();
    }
    void getPostById(@NonNull String postId, @NonNull FirestoreGetPostByIdCallbacks callbacks);


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

    /**
     * Fetches posts from Firestore for map display.
     * @param currentUserId The ID of the currently logged-in user, to potentially exclude their posts.
     * @return LiveData holding a list of Post objects.
     */
    LiveData<List<Post>> getPostsForMap(@Nullable String currentUserId);

}
