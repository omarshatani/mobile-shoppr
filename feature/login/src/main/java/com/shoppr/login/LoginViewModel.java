package com.shoppr.login;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.shoppr.navigation.NavigationRoute;
import com.shoppr.navigation.Navigator;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

	private final Navigator navigator;
	private static final String TAG = "LoginViewModel";

	@Inject
	public LoginViewModel(Navigator navigator) {
		this.navigator = navigator;
	}

	public void onSignInSuccess() {
		Log.i(TAG, "Sign in successful, requesting navigation to Map.");
		// Request navigation to the main app screen
		navigator.navigate(new NavigationRoute.LoginToMap());
	}

	public void onSignInFailed(String errorMessage) {
		Log.w(TAG, "Sign in failed: " + errorMessage);
		// Potentially update some LiveData here to show an error message in the Fragment
		// For now, we do nothing, user stays on login screen
	}

	public void onSignInCancelled() {
		Log.w(TAG, "Sign in cancelled.");
		// Optional: Navigate back if cancellation should exit the login flow?
		// navigator.goBack();
		// For now, do nothing, user stays on login screen
	}
}