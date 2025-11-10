package com.shoppr.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.shoppr.login.databinding.FragmentLoginBinding;
import com.shoppr.navigation.NavigationRoute;
import com.shoppr.navigation.Navigator;
import com.shoppr.ui.BaseFragment;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends BaseFragment<FragmentLoginBinding> {
	private static final String TAG = "LoginFragment";

	private LoginViewModel viewModel;
	private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
			new FirebaseAuthUIActivityResultContract(),
			this::onSignInResult
	);
	private boolean hasLaunchedSignIn = false;

	@Inject
	Navigator navigator;

	public LoginFragment() {
	}

	@Override
	protected FragmentLoginBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		return FragmentLoginBinding.inflate(inflater, container, false);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
	}


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		observeViewModel();
	}

	@Override
	protected InsetType getInsetType() {
		return InsetType.TOP;
	}

	@Override
	public void onStart() {
		super.onStart();
		viewModel.startObserving();
	}

	@Override
	public void onStop() {
		super.onStop();
		viewModel.stopObserving();
	}

	private void observeViewModel() {
		viewModel.authState.observe(getViewLifecycleOwner(), user -> {
			if (user != null) {
				viewModel.onUserAuthenticated(user);
			} else {
				if (!hasLaunchedSignIn) {
					launchSignInFlow();
				}
			}
		});

		viewModel.getNavigationCommand().observe(getViewLifecycleOwner(), event -> {
			if (event == null) {
				return;
			}

			NavigationRoute route = event.getContentIfNotHandled();
			if (route != null) {
				Log.d(TAG, "LoginFragment: Handling and navigating to " + route.getClass().getSimpleName());
				navigator.navigate(route);
				viewModel.onNavigationComplete();
			}
		});

		viewModel.getSignInFlowFeedbackMessage().observe(getViewLifecycleOwner(), event -> {
			String message = event.getContentIfNotHandled();
			if (message != null && !message.isEmpty()) {
				Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
			}
		});

		viewModel.getOperationErrorEvents().observe(getViewLifecycleOwner(), event -> {
			String errorMessage = event.getContentIfNotHandled();
			if (errorMessage != null) {
				Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
			}
		});
	}

	private void launchSignInFlow() {
		hasLaunchedSignIn = true;
		List<AuthUI.IdpConfig> providers = Arrays.asList(
				new AuthUI.IdpConfig.EmailBuilder().build(),
				new AuthUI.IdpConfig.GoogleBuilder().build());
		Intent signInIntent = AuthUI.getInstance()
				.createSignInIntentBuilder()
				.setAvailableProviders(providers)
				.setLogo(com.shoppr.core.ui.R.mipmap.ic_launcher)
				.setTheme(com.shoppr.core.ui.R.style.Theme_Shoppr)
				.build();
		signInLauncher.launch(signInIntent);
	}

	private void onSignInResult(@NonNull FirebaseAuthUIAuthenticationResult result) {
		viewModel.processSignInResult(result);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
}