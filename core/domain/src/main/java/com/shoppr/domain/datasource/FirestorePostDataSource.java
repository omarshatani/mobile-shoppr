package com.shoppr.domain.datasource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.shoppr.model.Post;

import java.util.List;

public interface FirestorePostDataSource {

    /**
     * Callbacks for operations that return a single Post.
     */
    interface PostOperationCallbacks {
        void onSuccess(@NonNull Post post);
        void onError(@NonNull String message);
        void onNotFound();
    }

    /**
     * Callbacks for simple success/error operations.
     */
    interface GeneralCallbacks {
        void onSuccess();
        void onError(@NonNull String message);
    }

    /**
     * Fetches all posts for a general feed, excluding posts by the current user.
     */
    LiveData<List<Post>> getFeedPosts(@Nullable String currentUserIdToExclude);

    /**
     * Fetches all posts created by a specific user.
     */
    LiveData<List<Post>> getPostsForUser(@NonNull String userId);

    /**
     * Fetches a list of posts based on a list of post IDs.
     */
    LiveData<List<Post>> getPostsByIds(@NonNull List<String> postIds);

    /**
     * Fetches a single post by its ID.
     */
    void getPostById(@NonNull String postId, @NonNull PostOperationCallbacks callbacks);

    /**
     * Creates a new post document in Firestore.
     */
    void createPost(@NonNull Post post, @NonNull PostOperationCallbacks callbacks);
}