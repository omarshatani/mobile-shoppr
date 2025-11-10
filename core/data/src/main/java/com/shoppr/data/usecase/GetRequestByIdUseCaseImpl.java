package com.shoppr.data.usecase;

import androidx.lifecycle.LiveData;

import com.shoppr.domain.repository.RequestRepository;
import com.shoppr.domain.usecase.GetRequestByIdUseCase;
import com.shoppr.model.Request;

import javax.inject.Inject;

public class GetRequestByIdUseCaseImpl implements GetRequestByIdUseCase {

	private final RequestRepository requestRepository;

	@Inject
	public GetRequestByIdUseCaseImpl(RequestRepository requestRepository) {
		this.requestRepository = requestRepository;
	}

	@Override
	public LiveData<Request> execute(String requestId) {
		return requestRepository.getRequestById(requestId);
	}
}