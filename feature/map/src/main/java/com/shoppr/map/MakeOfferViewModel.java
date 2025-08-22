package com.shoppr.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.DeleteOfferUseCase;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetRequestForPostUseCase;
import com.shoppr.domain.usecase.MakeOfferUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.Post;
import com.shoppr.model.Request;
import com.shoppr.model.RequestStatus;
import com.shoppr.model.User;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MakeOfferViewModel extends ViewModel {

	private final MakeOfferUseCase makeOfferUseCase;
	private final GetCurrentUserUseCase getCurrentUserUseCase;
	private final GetRequestForPostUseCase getRequestForPostUseCase;
	private final DeleteOfferUseCase deleteOfferUseCase;

	private final MutableLiveData<Request> _existingRequest = new MutableLiveData<>();

	public LiveData<Request> getExistingRequest() {
		return _existingRequest;
	}

	private final MutableLiveData<Event<Boolean>> _offerSubmittedEvent = new MutableLiveData<>();

	public LiveData<Event<Boolean>> getOfferSubmittedEvent() {
		return _offerSubmittedEvent;
	}

	private final MutableLiveData<Event<Boolean>> _offerWithdrawnEvent = new MutableLiveData<>();

	public LiveData<Event<Boolean>> getOfferWithdrawnEvent() {
		return _offerWithdrawnEvent;
	}

	private final MutableLiveData<Event<String>> _errorEvent = new MutableLiveData<>();

	public LiveData<Event<String>> getErrorEvent() {
		return _errorEvent;
	}

	@Inject
	public MakeOfferViewModel(MakeOfferUseCase makeOfferUseCase, GetCurrentUserUseCase getCurrentUserUseCase, GetRequestForPostUseCase getRequestForPostUseCase, DeleteOfferUseCase deleteOfferUseCase) {
		this.makeOfferUseCase = makeOfferUseCase;
		this.getCurrentUserUseCase = getCurrentUserUseCase;
		this.getRequestForPostUseCase = getRequestForPostUseCase;
		this.deleteOfferUseCase = deleteOfferUseCase;
	}

	public void loadExistingOffer(String postId) {
		User currentUser = getCurrentUserUseCase.getFullUserProfile().getValue();
		if (currentUser == null) {
			_errorEvent.setValue(new Event<>("Cannot load offer: User not found."));
			return;
		}

		getRequestForPostUseCase.execute(currentUser.getId(), postId, new GetRequestForPostUseCase.GetRequestForPostCallbacks() {
			@Override
			public void onSuccess(@Nullable Request request) {
				// Post the request to LiveData. It will be null if no offer exists.
				_existingRequest.setValue(request);
			}

			@Override
			public void onError(@NonNull String message) {
				_errorEvent.setValue(new Event<>(message));
			}
		});
	}

	public void submitOffer(Post post, String offerPrice, String note) {
		if (offerPrice == null || offerPrice.trim().isEmpty()) {
			_errorEvent.setValue(new Event<>("Offer price cannot be empty."));
			return;
		}

		User currentUser = getCurrentUserUseCase.getFullUserProfile().getValue();
		if (currentUser == null || post.getLister() == null) {
			_errorEvent.setValue(new Event<>("User or lister information is missing."));
			return;
		}

		try {
			Request offerToSubmit;
			Request existingOffer = _existingRequest.getValue();

			if (existingOffer != null) {
				// If an offer exists, we update its values
				existingOffer.setOfferAmount(Double.parseDouble(offerPrice));
				existingOffer.setMessage(note);
				offerToSubmit = existingOffer;
			} else {
				// Otherwise, we create a new one
				offerToSubmit = new Request.Builder()
						.postId(post.getId())
						.buyerId(currentUser.getId())
						.sellerId(post.getLister().getId())
						.offerAmount(Double.parseDouble(offerPrice))
						.offerCurrency(post.getCurrency())
						.message(note)
						.status(RequestStatus.PENDING)
						.build();
			}

			makeOfferUseCase.execute(offerToSubmit, new MakeOfferUseCase.MakeOfferCallbacks() {
				@Override
				public void onSuccess(@NonNull Request createdRequest) {
					_offerSubmittedEvent.setValue(new Event<>(true));
				}

				@Override
				public void onError(@NonNull String message) {
					_errorEvent.setValue(new Event<>(message));
				}
			});

		} catch (NumberFormatException e) {
			_errorEvent.setValue(new Event<>("Invalid offer price. Please enter a valid number."));
		}
	}

	public void withdrawOffer() {
		Request existingRequest = _existingRequest.getValue();
		if (existingRequest == null) {
			_errorEvent.setValue(new Event<>("Cannot withdraw: No offer found."));
			return;
		}

		deleteOfferUseCase.execute(existingRequest, new DeleteOfferUseCase.DeleteOfferCallbacks() {
			@Override
			public void onSuccess() {
				_offerWithdrawnEvent.setValue(new Event<>(true));
			}

			@Override
			public void onError(@NonNull String message) {
				_errorEvent.setValue(new Event<>(message));
			}
		});
	}
}