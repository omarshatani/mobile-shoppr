package com.shoppr.request;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.shoppr.model.Post;
import com.shoppr.model.Request;
import com.shoppr.request.databinding.FragmentRequestDetailBinding;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.utils.FormattingUtils;
import com.shoppr.ui.utils.ImageLoader;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RequestDetailFragment extends BaseFragment<FragmentRequestDetailBinding> {

	private RequestDetailViewModel viewModel;
	private com.shoppr.request.ActivityTimelineAdapter timelineAdapter;

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
		observeViewModel();
	}

	private void setupToolbar() {
		NavController navController = NavHostFragment.findNavController(this);
		NavigationUI.setupWithNavController(binding.toolbarRequestDetail, navController);
	}

	private void setupRecyclerView() {
		timelineAdapter = new com.shoppr.request.ActivityTimelineAdapter();
		binding.recyclerViewActivityTimeline.setAdapter(timelineAdapter);
		binding.recyclerViewActivityTimeline.setLayoutManager(new LinearLayoutManager(getContext()));
	}

	private void observeViewModel() {
		viewModel.getRequestDetails().observe(getViewLifecycleOwner(), uiModel -> {
			if (uiModel != null) {
				populateUI(uiModel);
			}
		});
	}

	private void populateUI(RequestUiModel uiModel) {
		Post post = uiModel.getPost();
		Request request = uiModel.getRequest();
		String currentUserId = viewModel.getCurrentUserId();

		boolean isMyOffer = currentUserId != null && currentUserId.equals(request.getBuyerId());

		if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
			ImageLoader.loadImage(binding.imagePostDetail, post.getImageUrl().get(0));
		}
		binding.textPostTitle.setText(post.getTitle());
		binding.textPostDescription.setText(post.getDescription());
		binding.textListPrice.setText(String.format("%s %s", FormattingUtils.formatPrice(post.getPrice()), post.getCurrency()));

		// Populate Categories
		binding.chipGroupCategory.removeAllViews();
		if (post.getCategories() != null && !post.getCategories().isEmpty()) {
			for (String categoryName : post.getCategories()) {
				Chip chip = new Chip(getContext());
				chip.setText(categoryName);
				binding.chipGroupCategory.addView(chip);
			}
		}

		if (post.getLister() != null) {
			binding.textListerUsername.setText(isMyOffer ? "You" : post.getLister().getName());
			binding.imageListerAvatar.setImageResource(com.shoppr.core.ui.R.drawable.ic_account_circle); // Using placeholder
		}


		binding.textOfferLabel.setText(isMyOffer ? "Your Offer" : "Their Offer");
		binding.textOfferPrice.setText(String.format("%s %s", FormattingUtils.formatPrice(request.getOfferAmount()), request.getOfferCurrency()));

		// Date in Offer Card - CORRECTED
		if (request.getCreatedAt() != null) {
			CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
					request.getCreatedAt().getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
			// This view ID `text_request_date` does not exist in the new layout.
			// The date is now part of the timeline, so we will handle it there.
			// We can remove this for now, or add a date to the offer card if you wish.
		}

		timelineAdapter.setCurrentUserId(currentUserId);

		// Submit the timeline data to the adapter
		if (request.getActivityTimeline() != null) {
			timelineAdapter.submitList(request.getActivityTimeline());
		}
	}

	@Override
	protected InsetType getInsetType() {
		return InsetType.TOP;
	}
}