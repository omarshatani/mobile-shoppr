package com.shoppr.profile;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shoppr.profile.databinding.FragmentProfileBinding;
import com.shoppr.ui.utils.InsetsUtils;

public class ProfileFragment extends Fragment {
	private static final String TAG = "ProfileFragment";
	private FragmentProfileBinding binding;
	private ProfileViewModel viewModel;

	public ProfileFragment() {}

	public static ProfileFragment newInstance() {
		return new ProfileFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
													 @Nullable Bundle savedInstanceState) {
		binding = FragmentProfileBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
			InsetsUtils.applySystemBarsAndImePadding(v, windowInsets);
			return WindowInsetsCompat.CONSUMED;
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		binding = null;
	}
}