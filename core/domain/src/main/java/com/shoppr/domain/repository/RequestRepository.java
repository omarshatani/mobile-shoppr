package com.shoppr.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.shoppr.model.Request;

import java.util.List;

public interface RequestRepository {

	interface RequestCreationCallbacks {
		void onSuccess(@NonNull Request createdRequest);
		void onError(String message);
	}

	interface RequestDeletionCallbacks {
		void onSuccess();

		void onError(@NonNull String message);
	}

	interface SingleRequestCallback {
		void onSuccess(@Nullable Request request);

		void onError(@NonNull String message);
	}

	LiveData<List<Request>> getRequestsForPost(@NonNull String postId);

	LiveData<List<Request>> getAllRequestsForUser(@NonNull String userId);

	void createRequest(@NonNull Request request, @NonNull RequestCreationCallbacks callback);

	void deleteRequest(@NonNull Request request, @NonNull RequestDeletionCallbacks callbacks);

	void getRequestForPost(String userId, String postId, SingleRequestCallback callbacks);
}