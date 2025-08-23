package com.shoppr.domain.datasource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.shoppr.model.Request;

import java.util.List;

public interface FirestoreRequestDataSource {

	interface RequestOperationCallbacks {
		void onSuccess(@NonNull Request request);

		void onError(@NonNull String message);
	}

	interface SingleRequestCallback {
		void onSuccess(@Nullable Request request);

		void onError(@NonNull String message);
	}

	interface RequestDeleteCallbacks {
		void onSuccess();

		void onError(@NonNull String message);
	}

	LiveData<List<Request>> getRequestsForPost(@NonNull String postId);

	LiveData<List<Request>> getAllRequestsForUser(@NonNull String userId);

	void createRequest(@NonNull Request request, @NonNull RequestOperationCallbacks callbacks);

	void deleteRequest(@NonNull Request request, @NonNull RequestDeleteCallbacks callbacks);

	void getRequestForPost(String userId, String postId, @NonNull SingleRequestCallback callbacks);
}