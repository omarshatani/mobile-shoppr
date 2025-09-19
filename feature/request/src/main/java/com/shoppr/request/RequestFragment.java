package com.shoppr.request;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shoppr.request.adapter.RequestsAdapter;
import com.shoppr.request.databinding.FragmentRequestBinding;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.FeedbackDialogFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RequestFragment extends BaseFragment<FragmentRequestBinding> implements RequestsAdapter.OnRequestClickListener {

	private RequestViewModel viewModel;
	private RequestsAdapter requestsAdapter;

	@Override
	protected FragmentRequestBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		return FragmentRequestBinding.inflate(inflater, container, false);
	}


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		viewModel = new ViewModelProvider(this).get(RequestViewModel.class);

		setupRecyclerView();
		observeViewModel();
		setupFragmentResultListener();
	}

	private void setupRecyclerView() {
		requestsAdapter = new RequestsAdapter(this); // Pass `this` as the listener
		binding.recyclerViewRequests.setLayoutManager(new LinearLayoutManager(getContext()));
		binding.recyclerViewRequests.setAdapter(requestsAdapter);
	}

	private void observeViewModel() {
		viewModel.currentUser.observe(getViewLifecycleOwner(), user -> {
			if (user != null) {
				requestsAdapter.setCurrentUserId(user.getId());
			}
		});

		viewModel.getRequests().observe(getViewLifecycleOwner(), requestUiModels -> {
			if (requestUiModels != null && !requestUiModels.isEmpty()) {
				requestsAdapter.submitList(requestUiModels);
				binding.recyclerViewRequests.setVisibility(View.VISIBLE);
				binding.viewEmptyState.setVisibility(View.GONE);
			} else {
				binding.recyclerViewRequests.setVisibility(View.GONE);
				binding.viewEmptyState.setVisibility(View.VISIBLE);
			}
		});
	}

	private void setupFragmentResultListener() {
		// Listen for a result from the CheckoutFragment.
		getParentFragmentManager().setFragmentResultListener("checkout_complete_key", getViewLifecycleOwner(), (requestKey, bundle) -> {
			// When the result is received, show the feedback dialog.
			String transactionId = bundle.getString("transactionId");
			String raterId = bundle.getString("raterId");
			String rateeId = bundle.getString("rateeId");
			String sellerName = bundle.getString("sellerName");

			if (transactionId != null && raterId != null && rateeId != null) {
				FeedbackDialogFragment.newInstance(transactionId, raterId, rateeId, sellerName)
						.show(getChildFragmentManager(), FeedbackDialogFragment.TAG);
			}
		});
	}

	@Override
	public InsetType getInsetType() {
		return InsetType.TOP_AND_BOTTOM;
	}

	@Override
	public void onRequestClicked(RequestUiModel requestUiModel) {
		String requestId = requestUiModel.getRequest().getId();
		NavHostFragment.findNavController(this).navigate(RequestFragmentDirections.actionRequestFragmentToRequestDetailFragment(requestId));
	}

}