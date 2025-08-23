package com.shoppr.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.shoppr.checkout.databinding.FragmentCheckoutBinding;
import com.shoppr.ui.BaseFragment;

public class CheckoutFragment extends BaseFragment<FragmentCheckoutBinding> {

	private CheckoutViewModel viewModel;

	public CheckoutFragment() {

	}

	public static CheckoutFragment newInstance() {
		return new CheckoutFragment();
	}

	@Override
	protected FragmentCheckoutBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		return FragmentCheckoutBinding.inflate(inflater, container, false);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	protected InsetType getInsetType() {
		return InsetType.TOP_AND_BOTTOM;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}