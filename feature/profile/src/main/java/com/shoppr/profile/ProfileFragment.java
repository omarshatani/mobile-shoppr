package com.shoppr.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.shoppr.navigation.NavigationRoute;
import com.shoppr.navigation.Navigator;
import com.shoppr.profile.databinding.FragmentProfileBinding;
import com.shoppr.ui.BaseFragment;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends BaseFragment {
	private static final String TAG = "ProfileFragment";
	private FragmentProfileBinding binding;
	private ProfileViewModel viewModel;
	private NavController localNavigator;
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

		setupLocalNavigation();
		setupClickListeners();
		observeViewModel();
	}

	@Override
	public InsetType getInsetType() {
		return InsetType.TOP;
	}

	@Override
	public void onStart() {
		super.onStart();
		viewModel.onFragmentStarted();
	}

	@Override
	public void onStop() {
		super.onStop();
		viewModel.onFragmentStopped();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private void setupLocalNavigation() {
		localNavigator = NavHostFragment.findNavController(this);
	}

	private void observeViewModel() {
		// Observer for the user's profile information
		viewModel.currentUserProfile.observe(getViewLifecycleOwner(), user -> {
			if (user != null) {
				// User is logged in, display their info
				binding.profileName.setText(user.getName());
				binding.profileEmail.setText(user.getEmail());
				// You could also load the profile image here using Glide
				binding.buttonLogout.setVisibility(View.VISIBLE);
			} else {
				// User is logged out, show guest info
				binding.profileName.setText(R.string.guest);
				binding.profileEmail.setText(R.string.user_logged_out_interaction_label);
				binding.buttonLogout.setVisibility(View.GONE);
			}
		});

		viewModel.getNavigationCommand().observe(getViewLifecycleOwner(), event -> {
			NavigationRoute route = event.getContentIfNotHandled();
			if (route == null) {
				return;
			}
			if (route instanceof NavigationRoute.ProfileToLogin) {
				navigator.navigate(route);
			} else if (route instanceof NavigationRoute.ProfileToFavorites) {
				localNavigator.navigate(route);
			}
		});
	}

	private void setupClickListeners() {
		binding.buttonLogout.setOnClickListener(v -> viewModel.onLogoutClicked());
		binding.menuItemMyFavorites.setOnClickListener(v -> viewModel.onMyFavoritesClicked());
	}
}