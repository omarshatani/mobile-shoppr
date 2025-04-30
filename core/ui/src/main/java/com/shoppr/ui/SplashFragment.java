package com.shoppr.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.shoppr.core.ui.R;
import com.shoppr.domain.CheckInitialNavigationUseCase;
import com.shoppr.navigation.Navigator;
import com.shoppr.ui.utils.Event;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashFragment extends Fragment {
    @Inject
    CheckInitialNavigationUseCase checkInitialNavigationUseCase;
    @Inject
    Navigator navigator;
    private static final String TAG = "SplashFragment";
    private SplashViewModel viewModel;

    public static SplashFragment newInstance() {
        return new SplashFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeNavigation();
    }

    private void observeNavigation() {
        viewModel.navigation.observe(getViewLifecycleOwner(), new Event.EventObserver<>(content -> {
            // This lambda is only called once per event
            Log.d(TAG, "Received navigation command: " + content.getClass().getSimpleName());
            if (navigator != null) {
                navigator.navigate(content);
            } else {
                Log.e(TAG, "Navigator was null, cannot execute navigation command.");
            }
            return null;
        }));
    }
}