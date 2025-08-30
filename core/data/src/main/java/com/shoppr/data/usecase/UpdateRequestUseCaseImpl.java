package com.shoppr.data.usecase;

import androidx.annotation.NonNull;

import com.shoppr.domain.repository.RequestRepository;
import com.shoppr.domain.usecase.UpdateRequestUseCase;
import com.shoppr.model.Request;

import javax.inject.Inject;

public class UpdateRequestUseCaseImpl implements UpdateRequestUseCase {

	private final RequestRepository requestRepository;

	@Inject
	public UpdateRequestUseCaseImpl(RequestRepository requestRepository) {
		this.requestRepository = requestRepository;
	}

	@Override
	public void execute(@NonNull Request request, @NonNull UpdateRequestCallbacks callbacks) {
		requestRepository.updateRequest(request, new RequestRepository.RequestUpdateCallbacks() {
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