package com.shoppr.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.shoppr.ui.utils.InsetUtils;

public abstract class BaseFragment extends Fragment {

	private static final String BASE_TAG = "BaseFragmentInsets";

	private androidx.core.view.OnApplyWindowInsetsListener decorViewListener = null;

	/**
	 * Subclasses should override this method and return false if they want to
	 * handle window inset padding themselves (e.g., MapFragment applying padding
	 * directly to the GoogleMap object). If true (default), the BaseFragment
	 * will apply padding to the fragment's root view.
	 *
	 * @return true if the BaseFragment should apply padding, false otherwise.
	 */
	protected boolean shouldApplyBaseInsetPadding() {
		// Default behavior is to apply padding
		return true;
	}

	protected boolean isLightStatusBarRequired() {
		// Default behavior assumes a light background, needing dark icons
		return true;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Only set up the listener if the subclass wants the base padding behavior
		if (shouldApplyBaseInsetPadding()) {
			setupEdgeToEdgeInsets();
		} else {
			Log.d(BASE_TAG, "Skipping base inset padding for: " + getClass().getSimpleName());
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(BASE_TAG, "onResume for: " + getClass().getSimpleName() + ", setting status bar appearance.");
		// Set the status bar appearance when the fragment becomes visible
		setSystemBarAppearance();
	}

	private void setupEdgeToEdgeInsets() {
		final String fragmentClassName = getClass().getSimpleName();
		final View decorView = requireActivity().getWindow().getDecorView();

		Log.d(BASE_TAG, "Setting up DecorView inset listener for: " + fragmentClassName);

		decorViewListener = (v, windowInsets) -> {
			View currentFragmentRootView = getView();
			if (currentFragmentRootView != null) {
				Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
				Log.d(BASE_TAG, "Listener on DecorView CALLED for " + fragmentClassName + "! Applying padding. Bottom: " + systemBars.bottom);
				// Apply padding to THIS FRAGMENT's root view
				InsetUtils.applySystemBarsAndImePadding(currentFragmentRootView, windowInsets);
				// currentFragmentRootView.requestLayout(); // Optional
			} else {
				Log.w(BASE_TAG, "Fragment view was null in DecorView listener for " + fragmentClassName);
			}
			return windowInsets;
		};

		ViewCompat.setOnApplyWindowInsetsListener(decorView, decorViewListener);

		getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleEventObserver() {
			@Override
			public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
				if (event == Lifecycle.Event.ON_DESTROY) {
					if (decorViewListener != null) {
						Log.d(BASE_TAG, "Removing DecorView inset listener for: " + fragmentClassName);
						ViewCompat.setOnApplyWindowInsetsListener(decorView, null);
						decorViewListener = null;
					}
					source.getLifecycle().removeObserver(this);
				}
			}
		});

		Log.d(BASE_TAG, "Requesting apply insets on DecorView for: " + fragmentClassName);
		ViewCompat.requestApplyInsets(decorView);

	}


	private void setSystemBarAppearance() {
		if (getActivity() != null) {
			Window window = getActivity().getWindow();
			// WindowInsetsControllerCompat requires API 16+ (getView() requires API 1)
			// Need a view to get the controller, use the fragment's view
			View view = getView();
			if (view != null) {
				WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, view);
				boolean lightStatusBar = isLightStatusBarRequired();
				controller.setAppearanceLightStatusBars(lightStatusBar);
				controller.setAppearanceLightNavigationBars(lightStatusBar);
				Log.d(BASE_TAG, "Set AppearanceLightStatusBars to: " + lightStatusBar + " for " + getClass().getSimpleName());
			} else {
				Log.w(BASE_TAG, "Fragment view was null in setSystemBarAppearance for " + getClass().getSimpleName());
			}
		} else {
			Log.w(BASE_TAG, "Activity was null in setSystemBarAppearance for " + getClass().getSimpleName());
		}
	}

	@Override
	public void onDestroyView() {
		// Safeguard cleanup
		if (decorViewListener != null && getActivity() != null) {
			View decorView = getActivity().getWindow().getDecorView();
			ViewCompat.setOnApplyWindowInsetsListener(decorView, null);
			decorViewListener = null;
		}
		super.onDestroyView();
	}
}