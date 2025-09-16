package com.shoppr.checkout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.CreateTransactionUseCase;
import com.shoppr.domain.usecase.GetPostByIdUseCase;
import com.shoppr.domain.usecase.GetRequestByIdUseCase;
import com.shoppr.domain.usecase.GetUserByIdUseCase;
import com.shoppr.domain.usecase.UpdateRequestUseCase;
import com.shoppr.model.ActivityEntry;
import com.shoppr.model.Event;
import com.shoppr.model.PaymentMethod;
import com.shoppr.model.Post;
import com.shoppr.model.Request;
import com.shoppr.model.RequestStatus;
import com.shoppr.model.Transaction;
import com.shoppr.model.TransactionStatus;
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
	private final CreateTransactionUseCase createTransactionUseCase;
	private final SavedStateHandle savedStateHandle;

	private final MediatorLiveData<CheckoutState> _checkoutState = new MediatorLiveData<>();
	public LiveData<CheckoutState> getCheckoutState() {
		return _checkoutState;
	}

	private final MutableLiveData<Event<Boolean>> _purchaseCompleteEvent = new MutableLiveData<>();
	public LiveData<Event<Boolean>> getPurchaseCompleteEvent() {
		return _purchaseCompleteEvent;
	}

	private final MutableLiveData<Event<String>> _errorEvent = new MutableLiveData<>();

	public LiveData<Event<String>> getErrorEvent() {
		return _errorEvent;
	}

	@Inject
	public CheckoutViewModel(
			GetRequestByIdUseCase getRequestByIdUseCase,
			GetPostByIdUseCase getPostByIdUseCase,
			GetUserByIdUseCase getUserByIdUseCase, UpdateRequestUseCase updateRequestUseCase, CreateTransactionUseCase createTransactionUseCase,
			SavedStateHandle savedStateHandle) {
		this.getRequestByIdUseCase = getRequestByIdUseCase;
		this.getPostByIdUseCase = getPostByIdUseCase;
		this.getUserByIdUseCase = getUserByIdUseCase;
		this.updateRequestUseCase = updateRequestUseCase;
		this.createTransactionUseCase = createTransactionUseCase;
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

	public void confirmPurchase(PaymentMethod paymentMethod) {
		CheckoutState currentState = _checkoutState.getValue();
		if (currentState == null) {
			_errorEvent.setValue(new Event<>("Cannot complete purchase, data is missing."));
			return;
		}

		Transaction transaction = new Transaction.Builder()
				.requestId(currentState.getRequest().getId())
				.postId(currentState.getPost().getId())
				.buyerId(currentState.getRequest().getBuyerId())
				.sellerId(currentState.getSeller().getId())
				.amount(currentState.getRequest().getOfferAmount())
				.currency(currentState.getRequest().getOfferCurrency())
				.serviceFee(currentState.getServiceFee())
				.totalAmount(currentState.getTotalAmount())
				.paymentMethod(paymentMethod)
				.status(TransactionStatus.PROCESSING)
				.build();

		createTransactionUseCase.execute(transaction, new CreateTransactionUseCase.CreateTransactionCallbacks() {
			@Override
			public void onSuccess(@NonNull Transaction createdTransaction) {
				updateRequestToCompleted(currentState, createdTransaction);
			}

			@Override
			public void onError(@NonNull String message) {
				_errorEvent.setValue(new Event<>(message));
			}
		});
	}

	private void updateRequestToCompleted(CheckoutState state, Transaction transaction) {
		Request requestToUpdate = state.getRequest();

		String description = String.format("Paid %s via %s",
				FormattingUtils.formatCurrency(transaction.getCurrency(), transaction.getTotalAmount()),
				transaction.getPaymentMethod().toString().toLowerCase());

		ActivityEntry paymentEntry = new ActivityEntry(
				requestToUpdate.getBuyerId(),
				"You",
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
				_errorEvent.setValue(new Event<>(message));
			}
		});
	}
}