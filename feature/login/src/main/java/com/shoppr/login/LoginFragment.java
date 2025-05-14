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
import com.shoppr.ui.utils.Event;

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
		observeViewModel();

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
		viewModel.loggedInUserLiveData.observe(getViewLifecycleOwner(), user -> {
			if (user != null) {
				Log.d(TAG, "observeViewModel (loggedInUserLiveData): Observed User: " + user.getId() + " Name: " + user.getName());
			} else {
				Log.d(TAG, "observeViewModel (loggedInUserLiveData): Observed no User (signed out).");
				Event<NavigationRoute> currentNavEvent = viewModel.getNavigationCommand().getValue();
				boolean navigatingToMap = currentNavEvent != null && currentNavEvent.peekContent() instanceof NavigationRoute.LoginToMap;

				if (!hasLaunchedSignIn && !navigatingToMap) { // Check if already navigating to map
					Log.d(TAG, "User is null, sign-in not in progress and not currently navigating to map. LoginFragment is active.");
				}
			}
		});

		viewModel.getNavigationCommand().observe(getViewLifecycleOwner(), event -> {
			NavigationRoute route = event.peekContent(); // Peek first

			if (route instanceof NavigationRoute.LoginToMap) {
				// This is a route LoginFragment's ViewModel specifically initiates.
				NavigationRoute consumedRoute = event.getContentIfNotHandled(); // Now consume
				if (consumedRoute != null) {
					Log.d(TAG, "LoginFragment: Handling and navigating to LoginToMap.");
					navigator.navigate(consumedRoute);
					Toast.makeText(requireContext(), "Login Successful! Navigating to Map...", Toast.LENGTH_SHORT).show();
					// viewModel.onNavigationEventHandled(); // Call if your Event wrapper needs explicit reset notification
				} else {
					Log.d(TAG, "LoginFragment: LoginToMap event was already handled or is not for this observer instance.");
				}
			} else if (route != null) {
				// This LoginFragment should generally not handle other navigation routes
				// that are not specific to its own successful completion.
				// Global navigation (like to Login screen after logout from elsewhere)
				// should be handled by MainActivity observing MainViewModel.
				Log.w(TAG, "LoginFragment: LoginViewModel emitted a route (" + route.getClass().getSimpleName() + ") that this fragment is not specifically handling with UI feedback.");
				// If there's a generic navigation that *must* happen from here, consume and navigate:
				// NavigationRoute consumedRoute = event.getContentIfNotHandled();
				// if (consumedRoute != null) { appNavigator.navigate(consumedRoute); }
			}
		});

		viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
			String message = event.getContentIfNotHandled(); // Consume the event
			if (message != null && !message.isEmpty()) {
				Log.d(TAG, "observeViewModel (toastMessage): Received message: " + message);
				Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
				// viewModel.onToastMessageEventHandled(); // Call if your Event wrapper needs explicit reset notification
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

	private void onSignInResult(@NonNull FirebaseAuthUIAuthenticationResult result) {
		Log.d(TAG, "onSignInResult: Received result from FirebaseUI. ResultCode: " + result.getResultCode());
		hasLaunchedSignIn = false;
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