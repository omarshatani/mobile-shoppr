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
import com.shoppr.domain.usecase.HasUserGivenFeedbackUseCase;
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
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RequestDetailViewModel extends ViewModel {

	// --- Use Cases ---
	private final GetRequestByIdUseCase getRequestByIdUseCase;
	private final GetPostByIdUseCase getPostByIdUseCase;
	private final GetCurrentUserUseCase getCurrentUserUseCase;
	private final UpdateRequestUseCase updateRequestUseCase;
	private final HasUserGivenFeedbackUseCase hasUserGivenFeedbackUseCase;
	private final SavedStateHandle savedStateHandle;

	// --- LiveData Sources ---
	private LiveData<Request> requestSource;
	private LiveData<User> userSource;
	private final MutableLiveData<Post> postSource = new MutableLiveData<>();
	private LiveData<Boolean> feedbackCheckSource;

	// --- State & Events ---
	private final MediatorLiveData<RequestDetailState> _requestDetailState = new MediatorLiveData<>();

	public LiveData<RequestDetailState> getRequestDetailState() {
		return _requestDetailState;
	}

	// Other events...
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
			HasUserGivenFeedbackUseCase hasUserGivenFeedbackUseCase,
			SavedStateHandle savedStateHandle) {
		this.getRequestByIdUseCase = getRequestByIdUseCase;
		this.getPostByIdUseCase = getPostByIdUseCase;
		this.getCurrentUserUseCase = getCurrentUserUseCase;
		this.updateRequestUseCase = updateRequestUseCase;
		this.hasUserGivenFeedbackUseCase = hasUserGivenFeedbackUseCase;
		this.savedStateHandle = savedStateHandle;

		loadRequestDetails();
	}

	private void loadRequestDetails() {
		String requestId = savedStateHandle.get("requestId");
		if (requestId == null) {
			return;
		}

		requestSource = getRequestByIdUseCase.execute(requestId);
		userSource = getCurrentUserUseCase.getFullUserProfile();
		// Initialize feedback source with a non-null default before adding
		feedbackCheckSource = new MutableLiveData<>(false);

		// Add all sources needed for the final state
		_requestDetailState.addSource(requestSource, request -> fetchPostIfNeeded(request, userSource.getValue()));
		_requestDetailState.addSource(userSource, user -> fetchPostIfNeeded(requestSource.getValue(), user));
		_requestDetailState.addSource(postSource, post -> combineAllData());
		_requestDetailState.addSource(feedbackCheckSource, hasRated -> combineAllData());
	}

	// Trigger post fetch
	private void fetchPostIfNeeded(@Nullable Request request, @Nullable User user) {
		if (request != null && user != null && request.getPostId() != null) {
			Post currentPost = postSource.getValue();
			if (currentPost == null || !Objects.equals(currentPost.getId(), request.getPostId())) {
				getPostByIdUseCase.execute(request.getPostId(), new GetPostByIdUseCase.GetPostByIdCallbacks() {
					@Override
					public void onSuccess(@NonNull Post post) {
						postSource.setValue(post); // Triggers combineAllData via observer
						initializeFeedbackListener(request, user, post); // Init listener *after* post load
						// No need to explicitly call combineAllData here, postSource observer does it.
					}

					@Override
					public void onError(@NonNull String message) {
						_errorEvent.setValue(new Event<>(message));
					}

					@Override
					public void onNotFound() {
						_errorEvent.setValue(new Event<>("Not found"));
					}
				});
			} else {
				// If post already loaded, just ensure listener is correct for current state
				initializeFeedbackListener(request, user, currentPost);
			}
		}
	}

	// Initialize or reset the feedback listener source
	private void initializeFeedbackListener(Request request, User user, Post post) {
		boolean isSeller = user.getId().equals(post.getLister().getId());
		LiveData<Boolean> newSource;

		if (isSeller && request.getStatus() == RequestStatus.COMPLETED) {
			// Conditions met: get the *actual* listening LiveData
			newSource = hasUserGivenFeedbackUseCase.execute(request.getId(), user.getId());
		} else {
			// Conditions NOT met: use a LiveData that always holds 'false'
			newSource = new MutableLiveData<>(false);
		}

		// Only swap source if it's actually different
		if (feedbackCheckSource != newSource) {
			_requestDetailState.removeSource(feedbackCheckSource); // Remove old one
			feedbackCheckSource = newSource;
			_requestDetailState.addSource(feedbackCheckSource, hasRated -> combineAllData()); // Add new one
			// If the new source is the static 'false', trigger combine immediately
			if (newSource instanceof MutableLiveData && newSource.getValue() == Boolean.FALSE) {
				combineAllData();
			}
		} else {
			// If source is the same, still might need to recombine if request/user changed
			combineAllData();
		}
	}

	// Combine all available data - Called whenever ANY source changes
	private void combineAllData() {
		Request request = requestSource.getValue();
		User user = userSource.getValue();
		Post post = postSource.getValue();
		Boolean hasRated = feedbackCheckSource.getValue();

		if (request != null && user != null && post != null && hasRated != null) {
			_requestDetailState.setValue(new RequestDetailState(post, request, user, hasRated));
		}
	}

	public void acceptOffer() {
		RequestDetailState currentState = _requestDetailState.getValue();
		if (currentState == null) return;
		RequestStatus currentStatus = currentState.getRequest().getStatus();

		if (currentState.isCurrentUserSeller) {
			if (currentStatus == RequestStatus.SELLER_PENDING) {
				updateRequest(RequestStatus.SELLER_ACCEPTED, "Accepted the offer", null);
			} else if (currentStatus == RequestStatus.BUYER_ACCEPTED) {
				updateRequest(RequestStatus.SELLER_ACCEPTED, "Confirmed the deal", null);
			}
		} else if (currentState.isCurrentUserBuyer) {
			if (currentStatus == RequestStatus.BUYER_PENDING) {
				updateRequest(RequestStatus.BUYER_ACCEPTED, "Accepted the counter-offer", null);
			} else if (currentStatus == RequestStatus.SELLER_ACCEPTED) {
				_navigateToCheckoutEvent.setValue(new Event<>(true));
			}
		}
	}

	public void rejectOffer() {
		updateRequest(RequestStatus.REJECTED, "Rejected the offer", null);
	}

	public void editOffer(String newPrice) {
		RequestDetailState currentState = getRequestDetailState().getValue();
		if (currentState == null || !currentState.isCurrentUserBuyer || currentState.getRequest().getStatus() != RequestStatus.SELLER_PENDING) {
			_errorEvent.setValue(new Event<>("Cannot edit offer at this time."));
			return;
		}
		try {
			double newAmount = Double.parseDouble(newPrice);
			String description = String.format("Edited offer to %s",
					FormattingUtils.formatCurrency(currentState.getRequest().getOfferCurrency(), newAmount));
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
			RequestStatus nextStatus = currentState.isCurrentUserSeller ? RequestStatus.BUYER_PENDING : RequestStatus.SELLER_PENDING;
			updateRequest(nextStatus, description, newAmount);
		} catch (NumberFormatException e) {
			_errorEvent.setValue(new Event<>("Invalid price format."));
		}
	}

	private void updateRequest(RequestStatus newStatus, String activityDescription, @Nullable Double newOfferAmount) {
		RequestDetailState currentState = _requestDetailState.getValue();
		if (currentState == null) {
			return;
		}

		Request requestToUpdate = currentState.getRequest();
		User currentUser = currentState.getCurrentUser();

		ActivityEntry newEntry = new ActivityEntry(currentUser.getId(), currentUser.getName(), activityDescription);
		newEntry.setCreatedAt(new Date());
		List<ActivityEntry> newTimeline = new ArrayList<>(requestToUpdate.getActivityTimeline() != null ? requestToUpdate.getActivityTimeline() : new ArrayList<>());
		newTimeline.add(newEntry);

		requestToUpdate.setStatus(newStatus);
		requestToUpdate.setActivityTimeline(newTimeline);
		if (newOfferAmount != null) {
			requestToUpdate.setOfferAmount(newOfferAmount);
		}

		updateRequestUseCase.execute(requestToUpdate, new UpdateRequestUseCase.UpdateRequestCallbacks() {
			@Override
			public void onSuccess() {
				String successMessage;
				switch (newStatus) {
					case BUYER_ACCEPTED:
						successMessage = "accepted";
						break;
					case SELLER_ACCEPTED:
						successMessage = "confirmed";
						break;
					case REJECTED:
						successMessage = "rejected";
						break;
					case COMPLETED:
						successMessage = "completed";
						break;
					case BUYER_PENDING:
					case SELLER_PENDING:
						successMessage = "updated";
						break;
					default:
						successMessage = newStatus.toString().toLowerCase();
						break;
				}
				_actionSuccessEvent.setValue(new Event<>(successMessage));
			}

			@Override
			public void onError(@NonNull String message) {
				_errorEvent.setValue(new Event<>(message));
			}
		});
	}

	public String getCurrentUserId() {
		User currentUser = userSource.getValue();
		return (currentUser != null) ? currentUser.getId() : null;
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		// Clean up potentially leaked observers if observeForever was used
		// requestSource.removeObserver(...);
		// userSource.removeObserver(...);
	}
}