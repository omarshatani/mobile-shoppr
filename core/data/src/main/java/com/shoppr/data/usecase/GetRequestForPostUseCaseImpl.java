package com.shoppr.data.usecase;

import com.shoppr.domain.repository.RequestRepository;
import com.shoppr.domain.usecase.GetRequestForPostUseCase;

import javax.inject.Inject;

public class GetRequestForPostUseCaseImpl implements GetRequestForPostUseCase {

	private final RequestRepository requestRepository;

	@Inject
	public GetRequestForPostUseCaseImpl(RequestRepository requestRepository) {
		this.requestRepository = requestRepository;
	}

	@Override
	public void execute(String userId, String postId, GetRequestForPostCallbacks callbacks) {
		requestRepository.getRequestForPost(userId, postId, callbacks);
	}
}