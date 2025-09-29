package com.shoppr.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.shoppr.core.ui.R;
import com.shoppr.map.databinding.BottomSheetMakeOfferBinding;
import com.shoppr.model.Post;
import com.shoppr.ui.utils.FormattingUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MakeOfferBottomSheet extends BottomSheetDialogFragment {

	public static final String TAG = "MakeOfferBottomSheet";
	private static final String ARG_POST = "arg_post";

	private BottomSheetMakeOfferBinding binding;
	private MakeOfferViewModel viewModel;
	private Post post;

	public static MakeOfferBottomSheet newInstance(Post post) {
		MakeOfferBottomSheet fragment = new MakeOfferBottomSheet();
		Bundle args = new Bundle();
		args.putParcelable(ARG_POST, post);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_App_BottomSheetDialog);
		viewModel = new ViewModelProvider(this).get(MakeOfferViewModel.class);
		if (getArguments() != null) {
			post = getArguments().getParcelable(ARG_POST);
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = BottomSheetMakeOfferBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (post == null) {
			Toast.makeText(getContext(), "Error: Post data is missing.", Toast.LENGTH_SHORT).show();
			dismiss();
			return;
		}
		viewModel.loadExistingOffer(post.getId());

		setupUI();
		setupClickListeners();
		observeViewModel();
	}

	private void observeViewModel() {
		viewModel.getExistingRequest().observe(getViewLifecycleOwner(), existingRequest -> {
			if (existingRequest != null) {
				binding.editTextOfferPrice.setText(String.valueOf(existingRequest.getOfferAmount()));
				binding.editTextNote.setText(existingRequest.getMessage());
				binding.buttonSubmitOffer.setText(R.string.update_offer);
				binding.buttonWithdrawOffer.setVisibility(View.VISIBLE);
			} else {
				binding.buttonSubmitOffer.setText(R.string.submit_offer);
				binding.buttonWithdrawOffer.setVisibility(View.GONE);
			}
		});

		viewModel.getOfferSubmittedEvent().observe(getViewLifecycleOwner(), event -> {
			if (event.getContentIfNotHandled() != null) {
				Toast.makeText(getContext(), "Offer submitted successfully!", Toast.LENGTH_SHORT).show();
				dismiss();
			}
		});

		viewModel.getOfferWithdrawnEvent().observe(getViewLifecycleOwner(), event -> {
			if (event.getContentIfNotHandled() != null) {
				Toast.makeText(getContext(), "Offer withdrawn", Toast.LENGTH_SHORT).show();
				dismiss();
			}
		});

		viewModel.getErrorEvent().observe(getViewLifecycleOwner(), event -> {
			String errorMessage = event.getContentIfNotHandled();
			if (errorMessage != null) {
				Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
			}
		});
	}

	private void setupUI() {
		binding.textPostTitle.setText(post.getTitle());
		String basePriceText = String.format("%s", FormattingUtils.formatCurrency(post.getCurrency(), Double.parseDouble(post.getPrice())));
		binding.textBasePriceValue.setText(basePriceText);

		binding.chipGroupCategory.removeAllViews();
		if (post.getCategories() != null && !post.getCategories().isEmpty()) {
			binding.chipGroupCategory.setVisibility(View.VISIBLE);
			for (String categoryName : post.getCategories()) {
				com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(getContext());
				chip.setText(categoryName);
				binding.chipGroupCategory.addView(chip);
			}
		} else {
			binding.chipGroupCategory.setVisibility(View.GONE);
		}
	}

	private void setupClickListeners() {
		binding.buttonSubmitOffer.setOnClickListener(v -> {
			String offerPrice = binding.editTextOfferPrice.getText().toString();
			String note = binding.editTextNote.getText().toString();
			viewModel.submitOffer(post, offerPrice, note);
		});
		binding.buttonWithdrawOffer.setOnClickListener(v -> viewModel.withdrawOffer());
		binding.buttonCancel.setOnClickListener(v -> dismiss());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}