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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

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

	private static final String TAG = "LoginFragment";
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

	private void launchSignInFlow() {
		// Create and launch sign-in intent
		// Use requireContext() to get context from Fragment
		Intent signInIntent = AuthUI.getInstance() // Pass context here if needed by AuthUI internals
				.createSignInIntentBuilder()
				.setAvailableProviders(providers)
				// Optional: Set logo, theme, etc. (Use resources from your app/core:ui module if needed)
				// .setLogo(com.yourcompany.yourapp.core.ui.R.drawable.auth_logo)
				// .setTheme(com.yourcompany.yourapp.core.ui.R.style.AuthTheme)
				.build();

		Log.d(TAG, "Launching Sign-in Intent from Fragment");
		signInLauncher.launch(signInIntent);
	}

	private void onSignInResult(@NonNull FirebaseAuthUIAuthenticationResult result) {
		IdpResponse response = result.getIdpResponse();
		if (result.getResultCode() == getActivity().RESULT_OK) { // Use Activity's RESULT_OK
			// Successfully signed in
			FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // Get user instance
			if (user != null) {
				Log.d(TAG, "Sign-in successful! User: " + user.getUid());
				Toast.makeText(requireContext(), "Sign in successful!", Toast.LENGTH_SHORT).show();
				// --- Trigger Navigation ---
				navigateToMainApp();
			} else {
				Log.e(TAG, "Sign-in OK but user is null");
				Toast.makeText(requireContext(), "Sign-in successful but failed to get user.", Toast.LENGTH_SHORT).show();
			}

		} else {
			// Sign in failed. Handle errors
			if (response == null) {
				// User pressed back button
				Log.w(TAG, "Sign-in cancelled by user.");
				Toast.makeText(requireContext(), "Sign in cancelled", Toast.LENGTH_SHORT).show();
			} else if (response.getError() != null) {
				Log.e(TAG, "Sign-in error: ", response.getError());
				Toast.makeText(requireContext(), "Sign in failed: " + response.getError().getMessage(), Toast.LENGTH_LONG).show();
			} else {
				Log.e(TAG, "Sign-in failed with unknown error. Result code: " + result.getResultCode());
				Toast.makeText(requireContext(), "Sign in failed.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void navigateToMainApp() {
		// Use Jetpack Navigation Component to navigate away from Login
		// Ensure you have defined this action in your navigation graph (e.g., nav_graph.xml in app module's res/navigation)
		// <fragment android:id="@+id/loginFragment" ... >
		//      <action android:id="@+id/action_loginFragment_to_main_feature"
		//              app:destination="@id/main_feature_graph_or_fragment" // ID of where to go after login
		//              app:popUpTo="@id/loginFragment" // Remove login from backstack
		//              app:popUpToInclusive="true" />
		// </fragment>
		try {
			NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_main_feature); // Replace with your action ID
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Navigation failed. Action ID likely incorrect or NavController not found.", e);
			Toast.makeText(requireContext(), "Error navigating after login.", Toast.LENGTH_SHORT).show();
			// Handle error appropriately, maybe try navigating via Activity context if needed in edge cases.
		}
	}

	private AuthUI getAuthUI () {
		AuthUI authUI = AuthUI.getInstance();
		authUI.useEmulator("10.0.2.2", 9099); // TODO: verify if it's using an emulator

		return authUI;
	}
}