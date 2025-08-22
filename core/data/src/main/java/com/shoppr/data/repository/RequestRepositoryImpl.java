package com.shoppr.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.shoppr.domain.datasource.FirestoreRequestDataSource;
import com.shoppr.domain.repository.RequestRepository;
import com.shoppr.domain.usecase.GetRequestForPostUseCase;
import com.shoppr.model.Request;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RequestRepositoryImpl implements RequestRepository {

	private final FirestoreRequestDataSource firestoreRequestDataSource;

	@Inject
	public RequestRepositoryImpl(FirestoreRequestDataSource firestoreRequestDataSource) {
		this.firestoreRequestDataSource = firestoreRequestDataSource;
	}

	@Override
	public void createRequest(@NonNull Request request, @NonNull RequestCreationCallbacks callback) {
		firestoreRequestDataSource.createRequest(request, new FirestoreRequestDataSource.RequestOperationCallbacks() {
			@Override
			public void onSuccess(@NonNull Request createdRequest) {
				callback.onSuccess(createdRequest);
			}

			@Override
			public void onError(@NonNull String message) {
				callback.onError(message);
			}
		});
	}

	@Override
	public LiveData<List<Request>> getRequestsForPost(@NonNull String postId) {
		return firestoreRequestDataSource.getRequestsForPost(postId);
	}

	@Override
	public void getRequestForPost(String userId, String postId, GetRequestForPostUseCase.GetRequestForPostCallbacks callbacks) {
		firestoreRequestDataSource.getRequestForPost(userId, postId, new FirestoreRequestDataSource.SingleRequestCallback() {
			@Override
			public void onSuccess(@Nullable Request request) {
				callbacks.onSuccess(request);
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}
}