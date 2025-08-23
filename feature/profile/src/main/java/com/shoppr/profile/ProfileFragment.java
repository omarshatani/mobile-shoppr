package com.shoppr.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.shoppr.core.ui.R;
import com.shoppr.navigation.NavigationRoute;
import com.shoppr.navigation.Navigator;
import com.shoppr.profile.databinding.FragmentProfileBinding;
import com.shoppr.ui.BaseFragment;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {
	private static final String TAG = "ProfileFragment";
	private ProfileViewModel viewModel;
	private NavController localNavigator;
	@Inject
	Navigator navigator;

	public ProfileFragment() {
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
	}

	@Override
	protected FragmentProfileBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		return FragmentProfileBinding.inflate(inflater, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setupLocalNavigation();
		setupMenuViews();
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

	private void setupLocalNavigation() {
		localNavigator = NavHostFragment.findNavController(this);
	}

	private void setupMenuViews() {
		binding.menuPersonalInformation.menuItemIcon.setImageResource(R.drawable.ic_account_circle);
		binding.menuPersonalInformation.menuItemText.setText(R.string.menu_personal_information);

		binding.menuItemMyMessages.menuItemIcon.setImageResource(R.drawable.ic_inbox);
		binding.menuItemMyMessages.menuItemText.setText(R.string.menu_my_posts);

		binding.menuItemMyFavorites.menuItemIcon.setImageResource(R.drawable.ic_favorite_outline);
		binding.menuItemMyFavorites.menuItemText.setText(R.string.menu_favorites);

		binding.menuItemSettings.menuItemIcon.setImageResource(R.drawable.ic_settings);
		binding.menuItemSettings.menuItemText.setText(R.string.menu_settings);

		binding.menuItemHelp.menuItemIcon.setImageResource(R.drawable.ic_help);
		binding.menuItemHelp.menuItemText.setText(R.string.menu_help);
	}

	private void observeViewModel() {
		viewModel.currentUserProfile.observe(getViewLifecycleOwner(), user -> {
			if (user != null) {
				binding.profileName.setText(user.getName());
				binding.profileEmail.setText(user.getEmail());
				binding.buttonLogout.setVisibility(View.VISIBLE);
			} else {
				binding.profileName.setText(R.string.guest);
				binding.profileEmail.setText(com.shoppr.profile.R.string.user_logged_out_interaction_label);
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
				NavDirections directions = ProfileFragmentDirections.actionProfileToFavorites();
				localNavigator.navigate(directions);
			}
		});
	}

	private void setupClickListeners() {
		binding.buttonLogout.setOnClickListener(v -> viewModel.onLogoutClicked());
		binding.menuItemMyFavorites.menuItemRoot.setOnClickListener(v -> viewModel.onMyFavoritesClicked());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
}