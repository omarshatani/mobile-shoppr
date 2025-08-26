package com.shoppr.request;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetPostByIdUseCase;
import com.shoppr.domain.usecase.GetRequestByIdUseCase;
import com.shoppr.model.Post;
import com.shoppr.model.Request;
import com.shoppr.model.User;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RequestDetailViewModel extends ViewModel {

	private final GetRequestByIdUseCase getRequestByIdUseCase;
	private final GetPostByIdUseCase getPostByIdUseCase;
	private final GetCurrentUserUseCase getCurrentUserUseCase;
	private final SavedStateHandle savedStateHandle;

	private final MediatorLiveData<RequestUiModel> _requestDetails = new MediatorLiveData<>();

	public LiveData<RequestUiModel> getRequestDetails() {
		return _requestDetails;
	}

	@Inject
	public RequestDetailViewModel(
			GetRequestByIdUseCase getRequestByIdUseCase,
			GetPostByIdUseCase getPostByIdUseCase, GetCurrentUserUseCase getCurrentUserUseCase,
			SavedStateHandle savedStateHandle) {
		this.getRequestByIdUseCase = getRequestByIdUseCase;
		this.getPostByIdUseCase = getPostByIdUseCase;
		this.getCurrentUserUseCase = getCurrentUserUseCase;
		this.savedStateHandle = savedStateHandle;

		loadRequestDetails();
	}

	private void loadRequestDetails() {
		String requestId = savedStateHandle.get("requestId");
		if (requestId == null) return;

		LiveData<Request> requestSource = getRequestByIdUseCase.execute(requestId);

		_requestDetails.addSource(requestSource, request -> {
			if (request != null && request.getPostId() != null) {

				// Once we have the Request, we fetch the associated Post using the callback
				getPostByIdUseCase.execute(request.getPostId(), new GetPostByIdUseCase.GetPostByIdCallbacks() {
					@Override
					public void onSuccess(@NonNull Post post) {
						// When the Post is successfully fetched, we create our UI model
						// and set it on the LiveData.
						_requestDetails.setValue(new RequestUiModel(request, post));
					}

					@Override
					public void onError(@NonNull String message) {
						// Handle the error, maybe post a null value or an error event
						_requestDetails.setValue(null);
					}

					@Override
					public void onNotFound() {
						_requestDetails.setValue(null);
					}
				});
			}
		});
	}

	public String getCurrentUserId() {
		User currentUser = getCurrentUserUseCase.getFullUserProfile().getValue();
		return (currentUser != null) ? currentUser.getId() : null;
	}
}