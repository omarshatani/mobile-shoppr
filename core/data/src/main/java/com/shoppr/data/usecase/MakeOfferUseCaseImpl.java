package com.shoppr.data.usecase;

import androidx.annotation.NonNull;

import com.shoppr.domain.repository.RequestRepository;
import com.shoppr.domain.usecase.MakeOfferUseCase;
import com.shoppr.model.Request;

import javax.inject.Inject;

public class MakeOfferUseCaseImpl implements MakeOfferUseCase {

	private final RequestRepository requestRepository;

	@Inject
	public MakeOfferUseCaseImpl(RequestRepository requestRepository) {
		this.requestRepository = requestRepository;
	}

	@Override
	public void execute(@NonNull Request request, @NonNull MakeOfferCallbacks callbacks) {
		requestRepository.createRequest(request, new RequestRepository.RequestCreationCallbacks() {
			@Override
			public void onSuccess(@NonNull Request createdRequest) {
				callbacks.onSuccess(createdRequest);
			}

			@Override
			public void onError(String message) {
				callbacks.onError(message);
			}
		});
	}
}