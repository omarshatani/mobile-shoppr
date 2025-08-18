package com.shoppr.domain.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.shoppr.model.Request;

import java.util.List;

public interface RequestRepository {

	interface RequestCreationCallbacks {
		void onSuccess(@NonNull Request createdRequest);

		void onError(String message);
	}

	void createRequest(@NonNull Request request, @NonNull RequestCreationCallbacks callback);

	LiveData<List<Request>> getRequestsForPost(@NonNull String postId);
}