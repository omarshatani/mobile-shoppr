package com.shoppr.login;

import android.app.Activity;
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
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.shoppr.login.databinding.FragmentLoginBinding;
import com.shoppr.ui.BaseFragment;
import com.shoppr.ui.utils.InsetUtils;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends BaseFragment {
	private static final String TAG = "LoginFragment";

	// Get the ViewModel scoped to this Fragment
	private LoginViewModel viewModel;
	// Launcher setup remains the same
	private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
			new FirebaseAuthUIActivityResultContract(),
			this::onSignInResult
	);
	private FragmentLoginBinding binding;
	private boolean hasLaunchedSignIn = false;

	public LoginFragment() {}

	public static LoginFragment newInstance() {
		return new LoginFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Initialize ViewModel
		viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
													 @Nullable Bundle savedInstanceState) {
		binding = FragmentLoginBinding.inflate(inflater, container, false);

//		AuthUI authUI = AuthUI.getInstance();
//		authUI.useEmulator("10.0.2.2", 9099);

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Apply insets
//		ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
//			InsetUtils.applySystemBarInsetsAsPadding(v, windowInsets);
//			return windowInsets;
//		});

		if (!hasLaunchedSignIn) {
			launchSignInFlow();
			hasLaunchedSignIn = true;
		}
	}

	private void launchSignInFlow() {
		List<AuthUI.IdpConfig> providers = Arrays.asList(
				new AuthUI.IdpConfig.EmailBuilder().build(),
				new AuthUI.IdpConfig.GoogleBuilder().build());
		// Create and launch sign-in intent
		// Use requireContext() to get context from Fragment
		Intent signInIntent = AuthUI.getInstance() // Pass context here if needed by AuthUI internals
				.createSignInIntentBuilder()
				.setAvailableProviders(providers)
				.setLogo(R.mipmap.ic_launcher)
				.setTheme(com.shoppr.core.ui.R.style.Theme_Shoppr)
				.setTosAndPrivacyPolicyUrls(
						"https://yourcompany.com/terms.html",
						"https://yourcompany.com/privacy.html")
				.setCredentialManagerEnabled(true)
				.build();

		Log.d(TAG, "Launching Sign-in Intent from Fragment");
		signInLauncher.launch(signInIntent);
	}

	// Handles the result - Calls the ViewModel
	private void onSignInResult(@NonNull FirebaseAuthUIAuthenticationResult result) {
		IdpResponse response = result.getIdpResponse();
		hasLaunchedSignIn = false;

		if (result.getResultCode() == Activity.RESULT_OK) {
			// *** Tell the ViewModel about success ***
			viewModel.onSignInSuccess();
		} else {
			// Sign in failed or cancelled
			if (response == null) {
				// *** Tell the ViewModel about cancellation ***
				viewModel.onSignInCancelled();
				Toast.makeText(requireContext(), "Sign-in cancelled", Toast.LENGTH_SHORT).show();
			} else if (response.getError() != null) {
				// *** Tell the ViewModel about the error ***
				String errorMessage = response.getError().getMessage();
				viewModel.onSignInFailed(errorMessage != null ? errorMessage : "Unknown error");
				Toast.makeText(requireContext(), "Sign-in failed: " + ": " + errorMessage, Toast.LENGTH_LONG).show();
			} else {
				viewModel.onSignInFailed("Unknown error code: " + result.getResultCode());
				Toast.makeText(requireContext(), "Sign-in error", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private AuthUI getAuthUI() {
		AuthUI authUI = AuthUI.getInstance();
		authUI.useEmulator("10.0.2.2", 9099); // TODO: verify if it's using an emulator

		return authUI;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}