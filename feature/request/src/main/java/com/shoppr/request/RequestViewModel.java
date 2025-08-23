package com.shoppr.request;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.GetAllRequestsUseCase;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetPostByIdUseCase;
import com.shoppr.model.Post;
import com.shoppr.model.Request;
import com.shoppr.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RequestViewModel extends ViewModel {

	private final GetAllRequestsUseCase getAllRequestsUseCase;
	private final GetPostByIdUseCase getPostByIdUseCase;
	private final GetCurrentUserUseCase getCurrentUserUseCase;

	private final MediatorLiveData<List<RequestUiModel>> _requests = new MediatorLiveData<>();

	public LiveData<List<RequestUiModel>> getRequests() {
		return _requests;
	}

	public final LiveData<User> currentUser;

	@Inject
	public RequestViewModel(
			GetAllRequestsUseCase getAllRequestsUseCase,
			GetPostByIdUseCase getPostByIdUseCase,
			GetCurrentUserUseCase getCurrentUserUseCase) {
		this.getAllRequestsUseCase = getAllRequestsUseCase;
		this.getPostByIdUseCase = getPostByIdUseCase;
		this.getCurrentUserUseCase = getCurrentUserUseCase;
		this.currentUser = this.getCurrentUserUseCase.getFullUserProfile();

		loadRequests();
	}

	private void loadRequests() {
		// Observe the current user
		_requests.addSource(currentUser, user -> {
			if (user != null) {
				// Once we have the user, fetch their requests
				LiveData<List<Request>> requestsSource = getAllRequestsUseCase.execute(user.getId());
				_requests.addSource(requestsSource, requests -> {
					if (requests == null || requests.isEmpty()) {
						_requests.setValue(Collections.emptyList());
					} else {
						// When requests arrive, fetch the post details for each one
						fetchPostDetailsForRequests(requests);
					}
				});
			} else {
				_requests.setValue(Collections.emptyList());
			}
		});
	}

	private void fetchPostDetailsForRequests(List<Request> requests) {
		List<RequestUiModel> uiModels = new ArrayList<>();
		AtomicInteger counter = new AtomicInteger(requests.size());

		for (Request request : requests) {
			getPostByIdUseCase.execute(request.getPostId(), new GetPostByIdUseCase.GetPostByIdCallbacks() {
				@Override
				public void onSuccess(@NonNull Post post) {
					uiModels.add(new RequestUiModel(request, post));
					if (counter.decrementAndGet() == 0) {
						_requests.setValue(uiModels);
					}
				}

				@Override
				public void onError(@NonNull String message) {
					if (counter.decrementAndGet() == 0) {
						_requests.setValue(uiModels);
					}
				}

				@Override
				public void onNotFound() {
					// Handle the case where the post is not found
				}
			});
		}
	}
}