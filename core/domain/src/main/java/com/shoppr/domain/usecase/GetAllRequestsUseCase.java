package com.shoppr.domain.usecase;

import androidx.lifecycle.LiveData;

import com.shoppr.model.Request;

import java.util.List;

public interface GetAllRequestsUseCase {
	LiveData<List<Request>> execute(String userId);
}