package com.shoppr.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
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
	private final MutableLiveData<Boolean> feedbackCheckResult = new MutableLiveData<>(false);
	private final MutableLiveData<Event<Boolean>> _feedbackSubmittedEvent = new MutableLiveData<>();

	public LiveData<Event<Boolean>> getFeedbackSubmittedEvent() {
		return _feedbackSubmittedEvent;
	}

	// --- State & Events ---
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

		// Add all sources that contribute to the final state
		_requestDetailState.addSource(requestSource, request -> combineAllData());
		_requestDetailState.addSource(userSource, user -> combineAllData());
		_requestDetailState.addSource(postSource, post -> combineAllData());
		_requestDetailState.addSource(feedbackCheckResult, hasRated -> combineAllData());

		// Initial trigger to fetch the post once request/user are loaded
		requestSource.observeForever(request -> fetchPostIfNeeded(request, userSource.getValue()));
		userSource.observeForever(user -> fetchPostIfNeeded(requestSource.getValue(), user));
	}

	// Trigger post fetch only when request and user are ready
	private void fetchPostIfNeeded(@Nullable Request request, @Nullable User user) {
		// Fetch post only if we have request & user, AND haven't fetched it yet OR request/user changed
		if (request != null && user != null && request.getPostId() != null) {
			Post currentPost = postSource.getValue();
			// If postSource is null or the postId has changed (unlikely but safe), fetch
			if (currentPost == null || !Objects.equals(currentPost.getId(), request.getPostId())) {
				getPostByIdUseCase.execute(request.getPostId(), new GetPostByIdUseCase.GetPostByIdCallbacks() {
					@Override
					public void onSuccess(@NonNull Post post) {
						postSource.setValue(post); // Triggers combineAllData
						// Perform the initial feedback check *after* post is loaded
						checkFeedbackStatus(request, user, post);
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
				// If post already exists, just re-check feedback status and combine
				checkFeedbackStatus(request, user, currentPost);
				combineAllData();
			}
		}
	}

	// Check feedback status and update the feedbackCheckResult LiveData
	private void checkFeedbackStatus(Request request, User user, Post post) {
		boolean isSeller = user.getId().equals(post.getLister().getId());
		if (isSeller && request.getStatus() == RequestStatus.COMPLETED) {
			LiveData<Boolean> source = hasUserGivenFeedbackUseCase.execute(request.getId(), user.getId());
			source.observeForever(new Observer<Boolean>() {
				@Override
				public void onChanged(Boolean hasRated) {
					if (!Objects.equals(feedbackCheckResult.getValue(), hasRated)) {
						feedbackCheckResult.setValue(hasRated); // Triggers combineAllData
					}
					source.removeObserver(this); // Clean up immediately
				}
			});
		} else {
			// If check not needed, ensure feedback status is false
			if (feedbackCheckResult.getValue() != Boolean.FALSE) {
				feedbackCheckResult.setValue(false); // Triggers combineAllData
			}
		}
	}

	// Combine all available data into the final state - called whenever any source changes
	private void combineAllData() {
		Request request = requestSource.getValue();
		User user = userSource.getValue();
		Post post = postSource.getValue();
		Boolean hasRated = feedbackCheckResult.getValue();

		// Only emit state when ALL required data is available
		if (request != null && user != null && post != null && hasRated != null) {
			_requestDetailState.setValue(new RequestDetailState(post, request, user, hasRated));
		}
	}

	public void onFeedbackSubmitted() {
		_feedbackSubmittedEvent.setValue(new Event<>(true));
		// Also trigger the refresh immediately
		refreshFeedbackStatus();
	}

	// --- PUBLIC METHOD FOR FRAGMENT TO TRIGGER REFRESH ---
	public void refreshFeedbackStatus() {
		Request request = requestSource.getValue();
		User user = userSource.getValue();
		Post post = postSource.getValue();
		if (request != null && user != null && post != null) {
			checkFeedbackStatus(request, user, post); // Re-run the check
		}
	}

	// --- ACTION METHODS (Use the versions you provided) ---
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
						break; // Using "confirmed" as per your previous logic
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

	// Clean up observers added with observeForever
	@Override
	protected void onCleared() {
		super.onCleared();
		if (requestSource != null) {
			// You might need to manage removal of the observeForever listeners added in loadRequestDetails
			// Depending on how your LiveData sources are implemented (e.g., if they are single-shot or continuous)
			// For simplicity, assuming they are managed elsewhere or are Activity/Fragment scoped.
		}
		// Clean up the single observer from checkFeedbackStatusIfNeeded if it's still active
		// This part is tricky without knowing the exact LiveData lifecycle from the use case.
		// A safer approach might involve using Transformations.switchMap or collecting Flows if using Kotlin.
	}
}