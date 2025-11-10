package com.shoppr.domain.usecase;

import androidx.lifecycle.LiveData;

import com.shoppr.model.Request;

public interface GetRequestByIdUseCase {
	LiveData<Request> execute(String requestId);
}