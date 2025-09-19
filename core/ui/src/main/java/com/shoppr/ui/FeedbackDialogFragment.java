package com.shoppr.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shoppr.core.ui.databinding.DialogSubmitFeedbackBinding;
import com.shoppr.model.Feedback;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FeedbackDialogFragment extends DialogFragment {

	public static final String TAG = "FeedbackDialog";
	private static final String ARG_TRANSACTION_ID = "transaction_id";
	private static final String ARG_RATER_ID = "rater_id";
	private static final String ARG_RATEE_ID = "ratee_id";
	private static final String ARG_SELLER_NAME = "seller_name";

	private DialogSubmitFeedbackBinding binding;
	private FeedbackViewModel viewModel;

	public static FeedbackDialogFragment newInstance(String transactionId, String raterId, String rateeId, String sellerName) {
		FeedbackDialogFragment fragment = new FeedbackDialogFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TRANSACTION_ID, transactionId);
		args.putString(ARG_RATER_ID, raterId);
		args.putString(ARG_RATEE_ID, rateeId);
		args.putString(ARG_SELLER_NAME, sellerName);
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		binding = DialogSubmitFeedbackBinding.inflate(getLayoutInflater());
		viewModel = new ViewModelProvider(this).get(FeedbackViewModel.class);

		observeViewModel();
		populateUI();

		return new MaterialAlertDialogBuilder(requireContext())
				.setView(binding.getRoot())
				.setPositiveButton("Submit", (dialog, which) -> submitFeedback())
				.setNegativeButton("Cancel", null)
				.create();
	}

	private void observeViewModel() {
		viewModel.getFeedbackSubmittedEvent().observe(this, event -> {
			if (event.getContentIfNotHandled() != null) {
				Toast.makeText(getContext(), "Feedback submitted!", Toast.LENGTH_SHORT).show();
				dismiss();
			}
		});
	}

	private void populateUI() {
		if (getArguments() != null) {
			String sellerName = getArguments().getString(ARG_SELLER_NAME, "the seller");
			String title = "How was your experience with " + sellerName + "?";
			binding.textFeedbackTitle.setText(title);
		}
	}

	private void submitFeedback() {
		Bundle args = getArguments();
		if (args == null) return;

		Feedback feedback = new Feedback();
		feedback.setTransactionId(args.getString(ARG_TRANSACTION_ID));
		feedback.setRaterId(args.getString(ARG_RATER_ID));
		feedback.setRateeId(args.getString(ARG_RATEE_ID));
		feedback.setRating(binding.ratingBarFeedback.getRating());
		feedback.setComment(Objects.requireNonNull(binding.editTextFeedbackComment.getText()).toString());

		viewModel.submitFeedback(feedback);
	}
}