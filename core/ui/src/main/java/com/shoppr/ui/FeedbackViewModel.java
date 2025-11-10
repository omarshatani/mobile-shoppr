package com.shoppr.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shoppr.domain.usecase.SubmitFeedbackUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.Feedback;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FeedbackViewModel extends ViewModel {

	private final SubmitFeedbackUseCase submitFeedbackUseCase;

	private final MutableLiveData<Event<Boolean>> _feedbackSubmittedEvent = new MutableLiveData<>();

	public LiveData<Event<Boolean>> getFeedbackSubmittedEvent() {
		return _feedbackSubmittedEvent;
	}

	@Inject
	public FeedbackViewModel(SubmitFeedbackUseCase submitFeedbackUseCase) {
		this.submitFeedbackUseCase = submitFeedbackUseCase;
	}

	public void submitFeedback(Feedback feedback) {
		submitFeedbackUseCase.execute(feedback, new SubmitFeedbackUseCase.SubmitFeedbackCallbacks() {
			@Override
			public void onSuccess() {
				_feedbackSubmittedEvent.setValue(new Event<>(true));
			}

			@Override
			public void onError(@NonNull String message) {
				// Optionally handle error with another LiveData event
				_feedbackSubmittedEvent.setValue(new Event<>(false));
			}
		});
	}
}