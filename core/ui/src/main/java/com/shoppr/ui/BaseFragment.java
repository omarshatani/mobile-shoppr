package com.shoppr.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.shoppr.navigation.BottomNavManager;
import com.shoppr.ui.utils.InsetUtils;

public abstract class BaseFragment<T extends ViewBinding> extends Fragment {
	public final static String TAG = "BaseFragment";

	private T _binding;
	protected T binding;

	/**
	 * Subclasses must implement this method to provide the inflater for their specific binding class.
	 */
	protected abstract T inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		_binding = inflateBinding(inflater, container);
		binding = _binding;
		return binding.getRoot();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		_binding = null;
		binding = null;
	}

	public enum InsetType {
		NONE, TOP, BOTTOM, TOP_AND_BOTTOM
	}

	protected abstract InsetType getInsetType();

	protected boolean isLightStatusBarRequired() {
		if (getContext() != null) {
			return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
					!= Configuration.UI_MODE_NIGHT_YES;
		}
		return true;
	}

	protected boolean shouldHideBottomNav() {
		return false;
	}

	protected BottomNavManager findParentBottomNavManager() {
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
		BottomNavManager manager = findParentBottomNavManager();
		if (manager != null && shouldHideBottomNav()) {
			manager.setBottomNavVisibility(true);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		setSystemBarAppearance();
	}

	private void applyInsets(@NonNull View view) {
		switch (getInsetType()) {
			case TOP:
				InsetUtils.applyTopInsets(view);
				break;
			case BOTTOM:
				InsetUtils.applyBottomInsets(view);
				break;
			case TOP_AND_BOTTOM:
				InsetUtils.applyTopAndBottomInsets(view);
				break;
			case NONE:
			default:
				break;
		}
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
			}
		}
	}
}