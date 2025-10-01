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
				_errorEvent.setValue(new Event<>("Post not found."));
			}
		});
	}

	public void acceptOffer() {
		RequestDetailState currentState = _requestDetailState.getValue();
		if (currentState == null) return;

		RequestStatus currentStatus = currentState.getRequest().getStatus();

		if (currentState.isCurrentUserSeller) {
			if (currentStatus == RequestStatus.SELLER_PENDING) {
				// Seller accepts initial offer -> moves to SELLER_ACCEPTED for buyer's confirmation.
				updateRequest(RequestStatus.SELLER_ACCEPTED, "Accepted the offer", null);
			} else if (currentStatus == RequestStatus.BUYER_ACCEPTED) {
				// --- THIS IS THE FIX ---
				// Seller confirms the deal after buyer accepted a counter-offer -> moves to SELLER_ACCEPTED for buyer's confirmation.
				updateRequest(RequestStatus.SELLER_ACCEPTED, "Confirmed the deal", null);
			}
		} else if (currentState.isCurrentUserBuyer) {
			if (currentStatus == RequestStatus.BUYER_PENDING) {
				// Buyer accepts a counter-offer -> moves to BUYER_ACCEPTED for seller's confirmation.
				updateRequest(RequestStatus.BUYER_ACCEPTED, "Accepted the counter-offer", null);
			} else if (currentStatus == RequestStatus.SELLER_ACCEPTED) {
				// Buyer gives final confirmation -> Navigate to checkout.
				_navigateToCheckoutEvent.setValue(new Event<>(true));
			}
		}
	}

	public void rejectOffer() {
		// A rejection is a final state.
		updateRequest(RequestStatus.REJECTED, "Rejected the offer", null);
	}

	public void editOffer(String newPrice) {
		RequestDetailState currentState = getRequestDetailState().getValue();
		if (currentState == null) return;
		try {
			double newAmount = Double.parseDouble(newPrice);
			String description = String.format("Edited offer to %s",
					FormattingUtils.formatCurrency(currentState.getRequest().getOfferCurrency(), newAmount));
			// An edit by the buyer keeps the state as SELLER_PENDING.
			updateRequest(RequestStatus.SELLER_PENDING, description, newAmount);
		} catch (NumberFormatException e) {
			_errorEvent.setValue(new Event<>("Invalid price format."));
		}
	}

	public void counterOffer(String newPrice) {
		RequestDetailState currentState = _requestDetailState.getValue();
		if (currentState == null) return;
		try {
			double newAmount = Double.parseDouble(newPrice);
			String description = String.format("Countered with %s",
					FormattingUtils.formatCurrency(currentState.getRequest().getOfferCurrency(), newAmount));
			// A counter-offer always flips the turn.
			RequestStatus nextStatus = currentState.isCurrentUserSeller ? RequestStatus.BUYER_PENDING : RequestStatus.SELLER_PENDING;
			updateRequest(nextStatus, description, newAmount);
		} catch (NumberFormatException e) {
			_errorEvent.setValue(new Event<>("Invalid price format."));
		}
	}

	// The generic updateRequest method no longer needs to calculate the finalStatus.
	private void updateRequest(RequestStatus newStatus, String activityDescription, @Nullable Double newOfferAmount) {
		RequestDetailState currentState = _requestDetailState.getValue();
		if (currentState == null) {
			return;
		}

		Request requestToUpdate = currentState.getRequest();
		User currentUser = currentState.getCurrentUser();

		// Create the timeline entry
		ActivityEntry newEntry = new ActivityEntry(
				currentUser.getId(), currentUser.getName(), activityDescription);
		newEntry.setCreatedAt(new Date());
		List<ActivityEntry> newTimeline = new ArrayList<>(requestToUpdate.getActivityTimeline() != null ? requestToUpdate.getActivityTimeline() : new ArrayList<>());
		newTimeline.add(newEntry);

		// Update the request object
		requestToUpdate.setStatus(newStatus); // The correct status is passed in directly.
		requestToUpdate.setActivityTimeline(newTimeline);
		if (newOfferAmount != null) {
			requestToUpdate.setOfferAmount(newOfferAmount);
		}

		updateRequestUseCase.execute(requestToUpdate, new UpdateRequestUseCase.UpdateRequestCallbacks() {
			@Override
			public void onSuccess() {
				switch (newStatus) {
					case BUYER_ACCEPTED:
						_actionSuccessEvent.setValue(new Event<>("accepted"));
						break;
					case SELLER_ACCEPTED:
						_actionSuccessEvent.setValue(new Event<>("confirmed"));
						break;
					case REJECTED:
						_actionSuccessEvent.setValue(new Event<>("rejected"));
					case COMPLETED:
						_actionSuccessEvent.setValue(new Event<>("completed"));
						break;
					case BUYER_PENDING:
					case SELLER_PENDING:
						_actionSuccessEvent.setValue(new Event<>("updated"));
						break;
					default:
						break;
				}
			}

			@Override
			public void onError(@NonNull String message) {
				_errorEvent.setValue(new Event<>(message));
			}
		});
	}
}