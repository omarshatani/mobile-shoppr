package com.shoppr.data.usecase;

import androidx.lifecycle.LiveData;

import com.shoppr.domain.repository.RequestRepository;
import com.shoppr.domain.usecase.GetAllRequestsUseCase;
import com.shoppr.model.Request;

import java.util.List;

import javax.inject.Inject;

public class GetAllRequestsUseCaseImpl implements GetAllRequestsUseCase {

	private final RequestRepository requestRepository;

	@Inject
	public GetAllRequestsUseCaseImpl(RequestRepository requestRepository) {
		this.requestRepository = requestRepository;
	}

	@Override
	public LiveData<List<Request>> execute(String userId) {
		return requestRepository.getAllRequestsForUser(userId);
	}
}