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
		// ViewModel is now Hilt-managed, obtained via ViewModelProvider
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
		observeViewModel();

		// Launch sign-in only if the ViewModel indicates no logged-in user (via User LiveData)
		// and if we haven't tried launching already in this fragment instance.
		if (viewModel.loggedInUserLiveData.getValue() == null && !hasLaunchedSignIn) {
			Log.d(TAG, "onViewCreated: No domain user and sign-in not launched. Launching sign-in flow.");
			launchSignInFlow();
			hasLaunchedSignIn = true;
		} else if (viewModel.loggedInUserLiveData.getValue() != null) {
			Log.d(TAG, "onViewCreated: Domain user already present (" + viewModel.loggedInUserLiveData.getValue().getId() + "). Navigation should be handled by ViewModel.");
		} else if (hasLaunchedSignIn) {
			Log.d(TAG, "onViewCreated: Sign-in was launched, but still no domain user. Waiting for result or AuthState observation.");
		}
	}

	private void observeViewModel() {
		// Observe your domain user model
		viewModel.loggedInUserLiveData.observe(getViewLifecycleOwner(), user -> { // Changed from loggedInUser to user
			if (user != null) { // Changed from loggedInUser to user
				Log.d(TAG, "observeViewModel (loggedInUserLiveData): Observed User: " + user.getId() + " Name: " + user.getName());
				// Navigation is now primarily driven by the _navigationRoute LiveData,
				// which is triggered by the ObserveAuthStateUseCase after profile checks.
			} else {
				Log.d(TAG, "observeViewModel (loggedInUserLiveData): Observed no User (signed out).");
				// If user is null and we haven't just tried to sign in,
				// maybe ensure the sign-in flow is available or re-trigger if appropriate.
				if (!hasLaunchedSignIn && (viewModel.getNavigationRoute().getValue() == null || !(viewModel.getNavigationRoute().getValue() instanceof NavigationRoute.LoginToMap))) {
					// This condition is to avoid re-launching if we are already trying to log in or have just logged out.
					// Or if a navigation to map is pending (which means login was successful)
					Log.d(TAG, "User is null, sign-in not in progress. Ensuring login flow is active/available.");
					// Potentially re-call launchSignInFlow() if it's safe and makes sense in your UX.
					// For now, we assume the user stays on the login screen.
				}
			}
		});

		viewModel.getNavigationRoute().observe(getViewLifecycleOwner(), route -> {
			if (route == null) return;
			Log.d(TAG, "observeViewModel (navigationRoute): Received route: " + route.getClass().getSimpleName() + ". Attempting to navigate.");
			navigator.navigate(route);

			if (route instanceof NavigationRoute.LoginToMap) {
				Toast.makeText(requireContext(), "Login Successful! Navigating to Map...", Toast.LENGTH_SHORT).show();
			}
			viewModel.onNavigationComplete();
		});

		viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
			if (message != null && !message.isEmpty()) {
				Log.d(TAG, "observeViewModel (toastMessage): Received message: " + message);
				Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
				viewModel.onToastMessageShown();
			}
		});
	}

	private void launchSignInFlow() {
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

	// Fragment receives FirebaseAuthUIAuthenticationResult and passes it to ViewModel
	private void onSignInResult(@NonNull FirebaseAuthUIAuthenticationResult result) {
		Log.d(TAG, "onSignInResult: Received result from FirebaseUI. ResultCode: " + result.getResultCode());
		hasLaunchedSignIn = false; // Reset flag
		viewModel.processSignInResult(result); // ViewModel now handles this via a UseCase
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart: Telling ViewModel to start observing auth state.");
		viewModel.registerAuthStateListener(); // Tells ViewModel to have its UseCase start observing
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop: Telling ViewModel to stop observing auth state.");
		viewModel.unregisterAuthStateListener(); // Tells ViewModel to have its UseCase stop observing
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}