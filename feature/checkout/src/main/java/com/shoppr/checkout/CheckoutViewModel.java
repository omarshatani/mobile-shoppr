package com.shoppr.checkout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.GetPostByIdUseCase;
import com.shoppr.domain.usecase.GetRequestByIdUseCase;
import com.shoppr.domain.usecase.GetUserByIdUseCase;
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
public class CheckoutViewModel extends ViewModel {

	private final GetRequestByIdUseCase getRequestByIdUseCase;
	private final GetPostByIdUseCase getPostByIdUseCase;
	private final GetUserByIdUseCase getUserByIdUseCase;
	private final UpdateRequestUseCase updateRequestUseCase;
	private final SavedStateHandle savedStateHandle;

	private final MediatorLiveData<CheckoutState> _checkoutState = new MediatorLiveData<>();

	public LiveData<CheckoutState> getCheckoutState() {
		return _checkoutState;
	}

	private final MutableLiveData<Event<Boolean>> _purchaseCompleteEvent = new MutableLiveData<>();

	public LiveData<Event<Boolean>> getPurchaseCompleteEvent() {
		return _purchaseCompleteEvent;
	}

	@Inject
	public CheckoutViewModel(
			GetRequestByIdUseCase getRequestByIdUseCase,
			GetPostByIdUseCase getPostByIdUseCase,
			GetUserByIdUseCase getUserByIdUseCase, UpdateRequestUseCase updateRequestUseCase,
			SavedStateHandle savedStateHandle) {
		this.getRequestByIdUseCase = getRequestByIdUseCase;
		this.getPostByIdUseCase = getPostByIdUseCase;
		this.getUserByIdUseCase = getUserByIdUseCase;
		this.updateRequestUseCase = updateRequestUseCase;
		this.savedStateHandle = savedStateHandle;

		loadCheckoutDetails();
	}

	private void loadCheckoutDetails() {
		String requestId = savedStateHandle.get("requestId");
		if (requestId == null) return;

		LiveData<Request> requestSource = getRequestByIdUseCase.execute(requestId);

		_checkoutState.addSource(requestSource, request -> {
			if (request != null) {
				// Once we have the request, fetch the post and seller details
				fetchPostAndSeller(request);
			}
		});
	}

	private void fetchPostAndSeller(Request request) {
		getPostByIdUseCase.execute(request.getPostId(), new GetPostByIdUseCase.GetPostByIdCallbacks() {
			@Override
			public void onSuccess(@NonNull Post post) {
				// Now that we have the post, fetch the seller's details
				getUserByIdUseCase.execute(post.getLister().getId(), new GetUserByIdUseCase.GetUserByIdCallbacks() {
					@Override
					public void onSuccess(@Nullable User seller) {
						if (seller != null) {
							_checkoutState.setValue(new CheckoutState(post, request, seller));
						}
					}

					@Override
					public void onError(@NonNull String message) { /* Handle error */ }


				});
			}

			@Override
			public void onError(@NonNull String message) { /* Handle error */ }

			@Override
			public void onNotFound() {
				/* Handle not found */
			}
		});
	}

	public void confirmPurchase() {
		CheckoutState currentState = _checkoutState.getValue();
		if (currentState == null) {
			// Handle error
			return;
		}

		// --- Step 1: Process Payment (Placeholder) ---
		// TODO: Integrate with a real payment gateway like Stripe or Google Pay.
		// For now, we'll assume the payment is always successful.
		boolean paymentSuccessful = true;

		if (paymentSuccessful) {
			// --- Step 2: Update the Request Status to COMPLETED ---
			Request requestToUpdate = currentState.getRequest();

			String description = String.format("Paid %s",
					FormattingUtils.formatCurrency(requestToUpdate.getOfferCurrency(), currentState.getTotalAmount()));

			ActivityEntry paymentEntry = new ActivityEntry(
					requestToUpdate.getBuyerId(),
					"You", // We can improve this later by getting the buyer's name
					description);
			paymentEntry.setCreatedAt(new Date());

			List<ActivityEntry> newTimeline = new ArrayList<>(requestToUpdate.getActivityTimeline());
			newTimeline.add(paymentEntry);

			requestToUpdate.setStatus(RequestStatus.COMPLETED);
			requestToUpdate.setActivityTimeline(newTimeline);

			updateRequestUseCase.execute(requestToUpdate, new UpdateRequestUseCase.UpdateRequestCallbacks() {
				@Override
				public void onSuccess() {
					_purchaseCompleteEvent.setValue(new Event<>(true));
				}

				@Override
				public void onError(@NonNull String message) {
					// Handle error
				}
			});
		}
	}
}