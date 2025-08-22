package com.shoppr.data.usecase;

import androidx.annotation.NonNull;

import com.shoppr.domain.repository.RequestRepository;
import com.shoppr.domain.usecase.DeleteOfferUseCase;
import com.shoppr.model.Request;

import javax.inject.Inject;

public class DeleteOfferUseCaseImpl implements DeleteOfferUseCase {

	private final RequestRepository requestRepository;

	@Inject
	public DeleteOfferUseCaseImpl(RequestRepository requestRepository) {
		this.requestRepository = requestRepository;
	}

	@Override
	public void execute(@NonNull Request request, @NonNull DeleteOfferCallbacks callbacks) {
		requestRepository.deleteRequest(request, new RequestRepository.RequestDeletionCallbacks() {
			@Override
			public void onSuccess() {
				callbacks.onSuccess();
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}
}