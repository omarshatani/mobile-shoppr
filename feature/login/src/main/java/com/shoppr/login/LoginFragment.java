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
public class LoginFragment extends BaseFragment {
	private static final String TAG = "LoginFragment";

	private LoginViewModel viewModel;
	private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
			new FirebaseAuthUIActivityResultContract(),
			this::onSignInResult
	);
	private FragmentLoginBinding binding;
	private boolean hasLaunchedSignIn = false;

	@Inject
	Navigator navigator;

	public LoginFragment() {
	}

	public static LoginFragment newInstance() {
		return new LoginFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
													 @Nullable Bundle savedInstanceState) {
		binding = FragmentLoginBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// This prevents re-launching if the flow is already active or if user is already logged in.
		if (!hasLaunchedSignIn) {
			Log.d(TAG, "onViewCreated: No current user and sign-in flow not active. Launching sign-in flow.");
			launchSignInFlow();
		} else {
			Log.d(TAG, "onViewCreated: Sign-in flow is already active. Waiting for result.");
		}

		observeViewModel();
	}

	private void observeViewModel() {
		viewModel.getNavigationCommand().observe(getViewLifecycleOwner(), event -> {
			if (event == null) {
				return;
			}

			NavigationRoute route = event.peekContent();
			if (route instanceof NavigationRoute.LoginToMap) {
				NavigationRoute consumedRoute = event.getContentIfNotHandled();
				if (consumedRoute != null) {
					Log.d(TAG, "LoginFragment: Handling and navigating to LoginToMap.");
					Toast.makeText(requireContext(), "Login Successful! Navigating to Map...", Toast.LENGTH_SHORT).show();
					navigator.navigate(consumedRoute);
					viewModel.onNavigationComplete();
				}
			} else if (route != null) {
				Log.w(TAG, "LoginFragment: Received a navigation command (" + route.getClass().getSimpleName() + ") not specifically handled with UI feedback here.");
			}
		});

		viewModel.getSignInFlowToastMessage().observe(getViewLifecycleOwner(), event -> {
			if (event == null) {
				return;
			}

			String message = event.getContentIfNotHandled();
			if (message != null && !message.isEmpty()) {
				Log.d(TAG, "LoginFragment Observer (signInFlowToastMessage): " + message);
				Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
			}
		});

		viewModel.authenticationErrorEvents.observe(getViewLifecycleOwner(), event -> {
			if (event == null) {
				return;
			}

			String errorMessage = event.getContentIfNotHandled();
			if (errorMessage != null) {
				Log.e(TAG, "LoginFragment Observer (authenticationErrorEvents): Auth Error: " + errorMessage);
				Toast.makeText(requireContext(), "Authentication process error: " + errorMessage, Toast.LENGTH_LONG).show();
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
				.setTosAndPrivacyPolicyUrls(
						"[https://yourcompany.com/terms.html](https://yourcompany.com/terms.html)",
						"[https://yourcompany.com/privacy.html](https://yourcompany.com/privacy.html)")
				.build();
		Log.d(TAG, "launchSignInFlow: Launching Sign-in Intent from Fragment");
		signInLauncher.launch(signInIntent);
	}

	private void onSignInResult(@NonNull FirebaseAuthUIAuthenticationResult result) {
		Log.d(TAG, "onSignInResult: Received result from FirebaseUI. ResultCode: " + result.getResultCode());
		viewModel.processSignInResult(result);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart: Telling ViewModel to start observing auth state.");
		viewModel.registerAuthStateListener();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop: Telling ViewModel to stop observing auth state.");
		viewModel.unregisterAuthStateListener();
	}
}