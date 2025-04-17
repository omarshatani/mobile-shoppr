package com.shoppr.login;

import static android.app.Activity.RESULT_OK;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shoppr.login.databinding.FragmentLoginBinding;

import java.util.Arrays;
import java.util.List;

public class LoginFragment extends Fragment {

	private LoginViewModel viewModel;
	private FragmentLoginBinding binding;

	List<AuthUI.IdpConfig> providers = Arrays.asList(
			new AuthUI.IdpConfig.EmailBuilder().build(),
			new AuthUI.IdpConfig.PhoneBuilder().build(),
			new AuthUI.IdpConfig.GoogleBuilder().build());

	private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
			new FirebaseAuthUIActivityResultContract(),
			this::onSignInResult
	);

	public static LoginFragment newInstance() {
		return new LoginFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
													 @Nullable Bundle savedInstanceState) {
		binding = FragmentLoginBinding.inflate(inflater, container, false);

		AuthUI authUI = AuthUI.getInstance();
		authUI.useEmulator("10.0.2.2", 9099);

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
	}

	private void startSignInFlow() {
		Intent signInIntent = AuthUI.getInstance()
				.createSignInIntentBuilder()
				.setAvailableProviders(providers)
				.build();
		signInLauncher.launch(signInIntent);
	}

	private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
		IdpResponse response = result.getIdpResponse();
		if (result.getResultCode() == RESULT_OK) {
			// Successfully signed in
			FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			// ...
		} else {
			// Sign in failed. If response is null the user canceled the
			// sign-in flow using the back button. Otherwise check
			// response.getError().getErrorCode() and handle the error.
			// ...
		}
	}

	private AuthUI getAuthUI () {
		AuthUI authUI = AuthUI.getInstance();
		authUI.useEmulator("10.0.2.2", 9099); // TODO: verify if it's using an emulator

		return authUI;
	}
}