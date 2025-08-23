package com.shoppr.request;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shoppr.request.adapter.RequestsAdapter;
import com.shoppr.request.databinding.FragmentRequestBinding;
import com.shoppr.ui.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RequestFragment extends BaseFragment<FragmentRequestBinding> {

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
	}

	@Override
	public InsetType getInsetType() {
		return InsetType.TOP_AND_BOTTOM;
	}

	private void setupRecyclerView() {
		requestsAdapter = new RequestsAdapter();
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
}