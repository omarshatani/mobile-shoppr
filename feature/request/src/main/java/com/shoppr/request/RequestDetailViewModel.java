package com.shoppr.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetPostByIdUseCase;
import com.shoppr.domain.usecase.GetRequestByIdUseCase;
import com.shoppr.domain.usecase.UpdateRequestUseCase;
import com.shoppr.model.ActivityEntry;
import com.shoppr.model.Event;
import com.shoppr.model.Post;
import com.shoppr.model.Request;
import com.shoppr.model.RequestStatus;
import com.shoppr.model.User;
import com.shoppr.ui.utils.FormattingUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RequestDetailViewModel extends ViewModel {

	private final GetRequestByIdUseCase getRequestByIdUseCase;
	private final GetPostByIdUseCase getPostByIdUseCase;
	private final GetCurrentUserUseCase getCurrentUserUseCase;
	private final UpdateRequestUseCase updateRequestUseCase;
	private final SavedStateHandle savedStateHandle;

	private final MediatorLiveData<RequestDetailState> _requestDetailState = new MediatorLiveData<>();

	public LiveData<RequestDetailState> getRequestDetailState() {
		return _requestDetailState;
	}

	private final MutableLiveData<Event<String>> _actionSuccessEvent = new MutableLiveData<>();

	public LiveData<Event<String>> getActionSuccessEvent() {
		return _actionSuccessEvent;
	}

	private final MutableLiveData<Event<String>> _errorEvent = new MutableLiveData<>();

	public LiveData<Event<String>> getErrorEvent() {
		return _errorEvent;
	}

	private final MutableLiveData<Event<Boolean>> _navigateToCheckoutEvent = new MutableLiveData<>();

	public LiveData<Event<Boolean>> getNavigateToCheckoutEvent() {
		return _navigateToCheckoutEvent;
	}

	@Inject
	public RequestDetailViewModel(
			GetRequestByIdUseCase getRequestByIdUseCase,
			GetPostByIdUseCase getPostByIdUseCase,
			GetCurrentUserUseCase getCurrentUserUseCase,
			UpdateRequestUseCase updateRequestUseCase,
			SavedStateHandle savedStateHandle) {
		this.getRequestByIdUseCase = getRequestByIdUseCase;
		this.getPostByIdUseCase = getPostByIdUseCase;
		this.getCurrentUserUseCase = getCurrentUserUseCase;
		this.updateRequestUseCase = updateRequestUseCase;
		this.savedStateHandle = savedStateHandle;

		loadRequestDetails();
	}

	private void loadRequestDetails() {
		String requestId = savedStateHandle.get("requestId");
		if (requestId == null) {
			_errorEvent.setValue(new Event<>("Request ID is missing."));
			return;
		}

		LiveData<Request> requestSource = getRequestByIdUseCase.execute(requestId);
		LiveData<User> userSource = getCurrentUserUseCase.getFullUserProfile();

		_requestDetailState.addSource(requestSource, request -> combineData(request, userSource.getValue()));
		_requestDetailState.addSource(userSource, user -> combineData(requestSource.getValue(), user));
	}

	private void combineData(@Nullable Request request, @Nullable User user) {
		if (request == null || user == null || request.getPostId() == null) {
			return;
		}

		getPostByIdUseCase.execute(request.getPostId(), new GetPostByIdUseCase.GetPostByIdCallbacks() {
			@Override
			public void onSuccess(@NonNull Post post) {
				_requestDetailState.setValue(new RequestDetailState(post, request, user));
			}

			@Override
			public void onError(@NonNull String message) {
				_errorEvent.setValue(new Event<>(message));
			}

			@Override
			public void onNotFound() {
				_errorEvent.setValue(new Event<>("Post not found"));
			}
		});
	}

	public void acceptOffer() {
		RequestDetailState currentState = _requestDetailState.getValue();
		if (currentState == null) return;

		RequestStatus currentStatus = currentState.getRequest().getStatus();

		if (currentState.isCurrentUserSeller) {
			if (currentStatus == RequestStatus.PENDING) {
				updateRequest(RequestStatus.ACCEPTED, "Seller accepted the offer", null);
			} else if (currentStatus == RequestStatus.ACCEPTED_COUNTERED) {
				updateRequest(RequestStatus.ACCEPTED, "Seller confirmed the deal", null);
			}
		} else if (currentState.isCurrentUserBuyer) {
			if (currentStatus == RequestStatus.COUNTERED) {
				updateRequest(RequestStatus.ACCEPTED_COUNTERED, "Buyer accepted the counter-offer", null);
			} else if (currentStatus == RequestStatus.ACCEPTED) {
				_navigateToCheckoutEvent.setValue(new Event<>(true));
			}
		}
	}

	public void editOffer(String newPrice) {
		RequestDetailState currentState = getRequestDetailState().getValue();
		if (currentState == null) return;
		try {
			double newAmount = Double.parseDouble(newPrice);
			String description = String.format("Edited offer to %s",
					FormattingUtils.formatCurrency(currentState.getRequest().getOfferCurrency(), newAmount));

			// An edit flips the turn, so the status is now the other person's to handle.
			RequestStatus nextStatus = currentState.isCurrentUserSeller ? RequestStatus.COUNTERED : RequestStatus.PENDING;

			updateRequest(nextStatus, description, newAmount);
		} catch (NumberFormatException e) {
			// Handle error
		}
	}

	public void rejectOffer() {
		RequestDetailState currentState = _requestDetailState.getValue();
		if (currentState == null) return;

		RequestStatus nextStatus = currentState.getRequest().getStatus() == RequestStatus.COUNTERED ?
				RequestStatus.REJECTED_COUNTERED : RequestStatus.REJECTED;

		updateRequest(nextStatus, "Rejected the offer", null);
	}

	public void counterOffer(String newPrice) {
		RequestDetailState currentState = _requestDetailState.getValue();
		if (currentState == null) return;
		try {
			double newAmount = Double.parseDouble(newPrice);
			String description = String.format("Countered with %s",
					FormattingUtils.formatCurrency(currentState.getRequest().getOfferCurrency(), newAmount));
			updateRequest(RequestStatus.COUNTERED, description, newAmount);
		} catch (NumberFormatException e) {
			_errorEvent.setValue(new Event<>("Invalid price format."));
		}
	}

	private void updateRequest(RequestStatus newStatus, String activityDescription, @Nullable Double newOfferAmount) {
		RequestDetailState currentState = _requestDetailState.getValue();
		if (currentState == null) {
			_errorEvent.setValue(new Event<>("Cannot perform action: current state is null."));
			return;
		}

		Request requestToUpdate = currentState.getRequest();
		User currentUser = currentState.getCurrentUser();
		RequestStatus finalStatus = newStatus;

		if (newStatus == RequestStatus.COUNTERED) {
			finalStatus = currentState.isCurrentUserSeller ? RequestStatus.COUNTERED : RequestStatus.PENDING;
		}

		ActivityEntry newEntry = new ActivityEntry(
				currentUser.getId(), currentUser.getName(), activityDescription);
		newEntry.setCreatedAt(new Date());

		List<ActivityEntry> newTimeline = new ArrayList<>(requestToUpdate.getActivityTimeline() != null ? requestToUpdate.getActivityTimeline() : new ArrayList<>());
		newTimeline.add(newEntry);

		requestToUpdate.setStatus(finalStatus);
		requestToUpdate.setActivityTimeline(newTimeline);
		if (newOfferAmount != null) {
			requestToUpdate.setOfferAmount(newOfferAmount);
		}

		updateRequestUseCase.execute(requestToUpdate, new UpdateRequestUseCase.UpdateRequestCallbacks() {
			@Override
			public void onSuccess() {
				_actionSuccessEvent.setValue(new Event<>(newStatus.toString().toLowerCase()));
			}

			@Override
			public void onError(@NonNull String message) {
				_errorEvent.setValue(new Event<>(message));
			}
		});
	}
}