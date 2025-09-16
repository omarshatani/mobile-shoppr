package com.shoppr.checkout;

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

import com.shoppr.checkout.databinding.FragmentCheckoutBinding;
import com.shoppr.model.Post;
import com.shoppr.model.Request;
import com.shoppr.model.User;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.utils.FormattingUtils;
import com.shoppr.ui.utils.ImageLoader;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CheckoutFragment extends BaseFragment<FragmentCheckoutBinding> {

	private CheckoutViewModel viewModel;

	@Override
	protected FragmentCheckoutBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		return FragmentCheckoutBinding.inflate(inflater, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);

		setupToolbar();
		setupClickListeners();
		setupPaymentMethodListener();
		observeViewModel();
	}

	private void setupToolbar() {
		NavController navController = NavHostFragment.findNavController(this);
		NavigationUI.setupWithNavController(binding.toolbarCheckout, navController);
	}

	private void setupPaymentMethodListener() {
		binding.radiogroupPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
			if (checkedId == R.id.radio_button_card) {
				// If card is selected, show the details view
				binding.layoutCardDetails.getRoot().setVisibility(View.VISIBLE);
			} else if (checkedId == R.id.radio_button_cash) {
				// If cash is selected, hide the details view
				binding.layoutCardDetails.getRoot().setVisibility(View.GONE);
			}
		});

		// Set the initial state
		binding.layoutCardDetails.getRoot().setVisibility(View.VISIBLE);
	}

	private void setupClickListeners() {
		binding.buttonConfirmPurchase.setOnClickListener(v -> {
			viewModel.confirmPurchase();
			// TODO: Handle navigation to a "Purchase Successful" screen or back to the map
			Toast.makeText(getContext(), "Purchase Confirmed!", Toast.LENGTH_SHORT).show();
		});
		binding.layoutCardDetails.buttonChangePayment.setOnClickListener(v -> {
			// TODO: Navigate to the "Add/Change Card" screen
			Toast.makeText(getContext(), "Change Payment Clicked!", Toast.LENGTH_SHORT).show();
		});
	}

	private void observeViewModel() {
		viewModel.getCheckoutState().observe(getViewLifecycleOwner(), state -> {
			if (state != null) {
				populateUI(state);
			} else {
				// Handle error state, e.g., show a message and pop back
				Toast.makeText(getContext(), "Could not load checkout details.", Toast.LENGTH_SHORT).show();
				NavHostFragment.findNavController(this).popBackStack();
			}
		});

		viewModel.getPurchaseCompleteEvent().observe(getViewLifecycleOwner(), event -> {
			if (event.getContentIfNotHandled() != null) {
				Toast.makeText(getContext(), "Purchase Confirmed!", Toast.LENGTH_LONG).show();
				// TODO: Navigate to a "My Purchases" screen or back to the map.
				// For now, just navigate back.
				NavHostFragment.findNavController(this).popBackStack();
			}
		});
	}

	private void populateUI(CheckoutState state) {
		Post post = state.getPost();
		Request request = state.getRequest();
		User seller = state.getSeller();

		// Item Summary
		if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
			ImageLoader.loadImage(binding.imagePostCheckout, post.getImageUrl().get(0));
		} else {
			// If there are no images, load a safe placeholder
			binding.imagePostCheckout.setImageResource(com.shoppr.core.ui.R.drawable.ic_placeholder_image);
		}
		binding.textPostTitleCheckout.setText(post.getTitle());
		binding.textFinalPriceCheckout.setText(
				FormattingUtils.formatCurrency(request.getOfferCurrency(), request.getOfferAmount())
		);

		// Seller & Meetup Info
		binding.textSellerUsername.setText(String.format("@%s", seller.getName()));
		binding.imageSellerAvatar.setImageResource(com.shoppr.core.ui.R.drawable.ic_account_circle);
		binding.editTextMeetupLocation.setText(post.getPostAddress()); // Default to lister's address

		// Payment Summary
		binding.textSubtotal.setText(
				FormattingUtils.formatCurrency(request.getOfferCurrency(), request.getOfferAmount())
		);
		binding.textServiceFee.setText(
				FormattingUtils.formatCurrency(request.getOfferCurrency(), state.getServiceFee())
		);
		binding.textTotalPrice.setText(
				FormattingUtils.formatCurrency(request.getOfferCurrency(), state.getTotalAmount())
		);

		// Footer Button
		String payButtonText = String.format("Pay %s",
				FormattingUtils.formatCurrency(request.getOfferCurrency(), state.getTotalAmount())
		);
		binding.buttonConfirmPurchase.setText(payButtonText);
	}

	@Override
	protected InsetType getInsetType() {
		return InsetType.TOP_AND_BOTTOM;
	}

	@Override
	protected boolean shouldHideBottomNav() {
		return true;
	}
}