package com.shoppr.request;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.shoppr.navigation.NavigationRoute;
import com.shoppr.navigation.Navigator;
import com.shoppr.request.adapter.ActivityTimelineAdapter;
import com.shoppr.request.databinding.FragmentRequestDetailBinding;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.utils.FormattingUtils;
import com.shoppr.ui.utils.ImageLoader;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RequestDetailFragment extends BaseFragment<FragmentRequestDetailBinding> {
	@Inject
	Navigator navigator;

	private RequestDetailViewModel viewModel;
	private ActivityTimelineAdapter timelineAdapter;

	@Override
	protected FragmentRequestDetailBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		return FragmentRequestDetailBinding.inflate(inflater, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(this).get(RequestDetailViewModel.class);

		setupToolbar();
		setupRecyclerView();
		setupClickListeners();
		observeViewModel();
	}

	private void setupToolbar() {
		NavController navController = NavHostFragment.findNavController(this);
		NavigationUI.setupWithNavController(binding.toolbarRequestDetail, navController);
	}

	private void setupRecyclerView() {
		timelineAdapter = new ActivityTimelineAdapter();
		binding.recyclerViewActivityTimeline.setAdapter(timelineAdapter);
		binding.recyclerViewActivityTimeline.setLayoutManager(new LinearLayoutManager(getContext()));
	}

	private void setupClickListeners() {
		binding.buttonAccept.setOnClickListener(v -> viewModel.acceptOffer());
		binding.buttonReject.setOnClickListener(v -> viewModel.rejectOffer());
		binding.buttonCounter.setOnClickListener(v -> showOfferDialog("Make a Counter Offer", false));
		binding.buttonEditOffer.setOnClickListener(v -> showOfferDialog("Edit Your Offer", true));
	}

	private void observeViewModel() {
		viewModel.getRequestDetailState().observe(getViewLifecycleOwner(), state -> {
			if (state != null) {
				populateUI(state);
			}
		});

		viewModel.getActionSuccessEvent().observe(getViewLifecycleOwner(), event -> {
			String status = event.getContentIfNotHandled();
			if (status != null) {
				Toast.makeText(getContext(), "Offer " + status, Toast.LENGTH_SHORT).show();
			}
		});

		viewModel.getErrorEvent().observe(getViewLifecycleOwner(), event -> {
			String message = event.getContentIfNotHandled();
			if (message != null) {
				Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
			}
		});

		viewModel.getNavigateToCheckoutEvent().observe(getViewLifecycleOwner(), event -> {
			if (event.getContentIfNotHandled() != null) {
				RequestDetailState currentState = viewModel.getRequestDetailState().getValue();
				if (currentState != null) {
					String requestId = currentState.getRequest().getId();
					Bundle args = new Bundle();
					args.putString("requestId", requestId);
					navigator.navigate(new NavigationRoute.RequestToCheckout(), args);
				}
			}
		});
	}

	private void populateUI(RequestDetailState state) {
		// Post Card
		if (state.getPost().getImageUrl() != null && !state.getPost().getImageUrl().isEmpty()) {
			ImageLoader.loadImage(binding.imagePostDetail, state.getPost().getImageUrl().get(0));
		} else {
			binding.imagePostDetail.setImageResource(com.shoppr.core.ui.R.drawable.ic_placeholder_image);
		}
		binding.textPostTitle.setText(state.getPost().getTitle());
		binding.textPostDescription.setText(state.getPost().getDescription());
		if (state.getPost().getPrice() != null) {
			binding.textListPrice.setText(
					FormattingUtils.formatCurrency(state.getPost().getCurrency(), Double.parseDouble(state.getPost().getPrice()))
			);
		}


		// Lister Info
		String listerName = state.isCurrentUserSeller ? "Your Listing" : String.format("@%s", state.getPost().getLister().getName());
		binding.textListerUsername.setText(String.format("%s", listerName));
		binding.imageListerAvatar.setImageResource(com.shoppr.core.ui.R.drawable.ic_account_circle);

		// Categories
		binding.chipGroupCategory.removeAllViews();
		if (state.getPost().getCategories() != null && !state.getPost().getCategories().isEmpty()) {
			for (String categoryName : state.getPost().getCategories()) {
				Chip chip = new Chip(getContext());
				chip.setText(categoryName);
				binding.chipGroupCategory.addView(chip);
			}
		}

		// Offer Card
		binding.textOfferPrice.setText(
				FormattingUtils.formatCurrency(state.getRequest().getOfferCurrency(), state.getRequest().getOfferAmount())
		);

		// Timeline
		timelineAdapter.setActorIds(
				state.getCurrentUser().getId(),
				state.getRequest().getBuyerId(),
				state.getPost().getLister().getId()
		);
		timelineAdapter.submitList(state.getRequest().getActivityTimeline());

		// Action Bar Visibility & Configuration
		binding.actionButtonBar.setVisibility(state.showActionButtons ? View.VISIBLE : View.GONE);
		if (state.showActionButtons) {
			binding.buttonAccept.setVisibility(state.showAcceptButton ? View.VISIBLE : View.GONE);
			binding.buttonReject.setVisibility(state.showRejectButton ? View.VISIBLE : View.GONE);
			binding.buttonCounter.setVisibility(state.showCounterButton ? View.VISIBLE : View.GONE);
			binding.buttonEditOffer.setVisibility(state.showEditOfferButton ? View.VISIBLE : View.GONE);
			binding.buttonAccept.setText(state.acceptButtonText);
		}
	}

	private void showOfferDialog(String title, boolean isEdit) {
		View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_counter_offer, null);
		TextInputEditText priceEditText = dialogView.findViewById(R.id.edit_text_counter_price);

		new MaterialAlertDialogBuilder(requireContext())
				.setTitle(title)
				.setView(dialogView)
				.setNegativeButton("Cancel", null)
				.setPositiveButton("Submit", (dialog, which) -> {
					String newPrice = priceEditText.getText().toString();
					if (!newPrice.isEmpty()) {
						if (isEdit) {
							viewModel.editOffer(newPrice);
						} else {
							viewModel.counterOffer(newPrice);
						}
					}
				})
				.show();
	}

	@Override
	protected InsetType getInsetType() {
		return InsetType.TOP;
	}

	@Override
	protected boolean shouldHideBottomNav() {
		return true;
	}
}