package com.shoppr.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.shoppr.map.databinding.BottomSheetMakeOfferBinding;
import com.shoppr.model.Post;

public class MakeOfferBottomSheet extends BottomSheetDialogFragment {

	public static final String TAG = "MakeOfferBottomSheet";
	private static final String ARG_POST = "arg_post";

	private BottomSheetMakeOfferBinding binding;
	private Post post;

	/**
	 * Creates a new instance of the bottom sheet and passes the post data.
	 *
	 * @param post The post to make an offer on.
	 * @return A new instance of MakeOfferBottomSheet.
	 */
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

		setStyle(BottomSheetDialogFragment.STYLE_NORMAL, com.shoppr.core.ui.R.style.ThemeOverlay_App_BottomSheetDialog);

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

		setupUI();
		setupClickListeners();
	}

	private void setupUI() {
		binding.textPostTitle.setText(post.getTitle());
		String basePriceText = String.format("%s %s", post.getPrice(), post.getCurrency());
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
			// TODO: Add logic to handle offer submission via a ViewModel
			String offerPrice = binding.editTextOfferPrice.getText().toString();
			String note = binding.editTextNote.getText().toString();

			// Simple validation for now
			if (offerPrice.isEmpty()) {
				binding.textInputLayoutOfferPrice.setError("Please enter an offer price.");
				return;
			}
			binding.textInputLayoutOfferPrice.setError(null);

			Toast.makeText(getContext(), "Offer of " + offerPrice + " submitted!", Toast.LENGTH_SHORT).show();
			dismiss(); // Close the bottom sheet on success
		});

		binding.buttonCancel.setOnClickListener(v -> {
			dismiss(); // Close the bottom sheet
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null; // Avoid memory leaks
	}
}