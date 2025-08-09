package com.shoppr.request;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.shoppr.request.databinding.FragmentRequestBinding;
import com.shoppr.ui.BaseFragment;

public class RequestFragment extends BaseFragment {

	private static final String TAG = "RequestFragment";
	private FragmentRequestBinding binding;
	private RequestViewModel viewModel;

	public static RequestFragment newInstance() {
		return new RequestFragment();
	}

	public RequestFragment() {}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(RequestViewModel.class);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
													 @Nullable Bundle savedInstanceState) {
		binding = FragmentRequestBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public InsetType getInsetType() {
		return InsetType.TOP_AND_BOTTOM;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		binding = null;
	}
}