package com.shoppr.request;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.shoppr.request.databinding.FragmentRequestBinding;
import com.shoppr.ui.utils.InsetsUtils;

public class RequestFragment extends Fragment {

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

		ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
			InsetsUtils.applySystemBarInsetsAsPadding(view, windowInsets);
			return WindowInsetsCompat.CONSUMED;
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		binding = null;
	}
}