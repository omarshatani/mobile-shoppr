package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.shoppr.model.Post;

import java.util.List;

public interface GetMyPostsUseCase {
    /**
     * Executes the logic to get posts created by a specific user.
     *
     * @param userId The ID of the user whose posts are to be fetched.
     * @return LiveData holding a list of Post objects.
     */
    LiveData<List<Post>> execute(@NonNull String userId);
}
