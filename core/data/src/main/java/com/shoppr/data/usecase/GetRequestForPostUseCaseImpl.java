package com.shoppr.data.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.domain.repository.RequestRepository;
import com.shoppr.domain.usecase.GetRequestForPostUseCase;
import com.shoppr.model.Request;

import javax.inject.Inject;

public class GetRequestForPostUseCaseImpl implements GetRequestForPostUseCase {

	private final RequestRepository requestRepository;

	@Inject
	public GetRequestForPostUseCaseImpl(RequestRepository requestRepository) {
		this.requestRepository = requestRepository;
	}

	@Override
	public void execute(String userId, String postId, GetRequestForPostCallbacks callbacks) {
		requestRepository.getRequestForPost(userId, postId, new RequestRepository.SingleRequestCallback() {
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