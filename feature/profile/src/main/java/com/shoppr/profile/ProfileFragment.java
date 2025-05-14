package com.shoppr.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.shoppr.navigation.Navigator;
import com.shoppr.profile.databinding.FragmentProfileBinding;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.utils.InsetUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends BaseFragment {
	private static final String TAG = "ProfileFragment";
	private FragmentProfileBinding binding;
	private ProfileViewModel viewModel;
	@Inject
	Navigator navigator;

	public ProfileFragment() {
	}

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
		setupRootViewInsets(view);
		setupUserName();
		setupEmail();
		setupLogoutCta();
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		binding = null;
	}

	private void setupRootViewInsets(View view) {
		ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
			InsetUtils.applyBottomNavPadding(
					v,
					windowInsets,
					com.shoppr.core.ui.R.dimen.bottom_nav_height
			);
			return windowInsets;
		});
		ViewCompat.requestApplyInsets(view);
	}

	private void setupUserName() {
		binding.profileName.setText("Omar");

	}

	private void setupEmail() {
		binding.profileEmail.setText("omar.shatani@gmail.com");
	}

	private void setupLogoutCta() {
		binding.buttonLogout.setOnClickListener(v -> viewModel.logout());
	}
}