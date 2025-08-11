package com.shoppr.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.shoppr.navigation.BottomNavManager;
import com.shoppr.ui.utils.InsetUtils;

public abstract class BaseFragment extends Fragment {
	public final static String TAG = "BaseFragment";

	/**
	 * Defines the type of system padding a fragment needs.
	 * This allows for a consistent approach to handling screen insets.
	 */
	public enum InsetType {
		NONE,           // For edge-to-edge screens like the map
		TOP,            // For screens that only need padding for the status bar
		BOTTOM,         // For screens that only need padding for the navigation bar
		TOP_AND_BOTTOM  // For standard screens that need both
	}

	/**
	 * Subclasses must override this method to declare what kind of insets they need.
	 *
	 * @return The InsetType for the fragment.
	 */
	protected abstract InsetType getInsetType();

	protected boolean isLightStatusBarRequired() {
		// Default behavior:
		// If system is in Dark Theme, we want LIGHT icons (return false).
		// If system is in Light Theme, we want DARK icons (return true).
		// This means we return true if the system is NOT in dark mode.
		if (getContext() != null) { // Ensure context is available to get resources
			return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
					!= Configuration.UI_MODE_NIGHT_YES;
		}
		// Fallback if context is not available (should ideally not happen when this is called)
		// Defaulting to true (dark icons) might be a "safer" visual fallback on many light themes.
		return true;
	}

	protected boolean shouldHideBottomNav() {
		return false;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		applyInsets(view);
	}

	@Override
	public void onStart() {
		super.onStart();
		BottomNavManager manager = findParentBottomNavManager();
		if (manager != null && shouldHideBottomNav()) {
			manager.setBottomNavVisibility(false);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		// Always ensure the nav bar is visible when leaving a full-screen fragment
		BottomNavManager manager = findParentBottomNavManager();
		if (manager != null && shouldHideBottomNav()) {
			manager.setBottomNavVisibility(true);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume for: " + getClass().getSimpleName() + ", setting status bar appearance.");
		// Set the status bar appearance when the fragment becomes visible
		setSystemBarAppearance();
	}

	private void applyInsets(@NonNull View view) {
		switch (getInsetType()) {
			case TOP:
				applyTopInsets(view);
				break;
			case BOTTOM:
				applyBottomInsets(view);
				break;
			case TOP_AND_BOTTOM:
				applyTopAndBottomInsets(view);
				break;
			case NONE:
			default:
				break;
		}
	}

	private void applyTopInsets(@NonNull View view) {
		InsetUtils.applyTopInsets(view);
	}

	private void applyBottomInsets(@NonNull View view) {
		InsetUtils.applyBottomInsets(view);
	}

	private void applyTopAndBottomInsets(@NonNull View view) {
		InsetUtils.applyTopAndBottomInsets(view);
	}

	private void setSystemBarAppearance() {
		if (getActivity() != null) {
			Window window = getActivity().getWindow();
			View view = getView();
			if (view != null) {
				WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, view);
				boolean lightStatusBar = isLightStatusBarRequired();
				controller.setAppearanceLightStatusBars(lightStatusBar);
				controller.setAppearanceLightNavigationBars(lightStatusBar);
				Log.d(TAG, "Set AppearanceLightStatusBars to: " + lightStatusBar + " for " + getClass().getSimpleName());
			} else {
				Log.w(TAG, "Fragment view was null in setSystemBarAppearance for " + getClass().getSimpleName());
			}
		} else {
			Log.w(TAG, "Activity was null in setSystemBarAppearance for " + getClass().getSimpleName());
		}
	}

	private BottomNavManager findParentBottomNavManager() {
		Fragment parent = getParentFragment();
		while (parent != null) {
			if (parent instanceof BottomNavManager) {
				return (BottomNavManager) parent;
			}
			parent = parent.getParentFragment();
		}
		if (getActivity() instanceof BottomNavManager) {
			return (BottomNavManager) getActivity();
		}
		return null;
	}
}