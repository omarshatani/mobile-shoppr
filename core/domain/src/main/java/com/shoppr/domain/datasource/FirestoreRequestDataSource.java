package com.shoppr.domain.datasource;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.shoppr.model.Request;

import java.util.List;

public interface FirestoreRequestDataSource {

	interface RequestOperationCallbacks {
		void onSuccess(@NonNull Request request);

		void onError(@NonNull String message);
	}

	void createRequest(@NonNull Request request, @NonNull RequestOperationCallbacks callbacks);

	LiveData<List<Request>> getRequestsForPost(@NonNull String postId);
}