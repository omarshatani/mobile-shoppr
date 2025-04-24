package com.shoppr.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewTreeLifecycleOwner;

import com.shoppr.core.ui.R;
import com.shoppr.domain.CheckInitialNavigationUseCase;
import com.shoppr.navigation.NavigationRoute;
import com.shoppr.navigation.Navigator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashFragment extends Fragment {
    @Inject
    CheckInitialNavigationUseCase checkInitialNavigationUseCase;
    @Inject
    Navigator appNavigator;
    private SplashViewModel mViewModel;
    // Flag to prevent multiple navigations
    private boolean navigated = false;

    public static SplashFragment newInstance() {
        return new SplashFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        // Use ViewTreeLifecycleOwner to observe lifecycle safely
        ViewTreeLifecycleOwner.get(view).getLifecycle().addObserver(new androidx.lifecycle.DefaultLifecycleObserver() {
            @Override
            public void onStart(@NonNull androidx.lifecycle.LifecycleOwner owner) {
                // Check auth state once the view is started and likely ready for navigation
                if (!navigated) {
                    checkAuthStateAndNavigate();
                }
            }
        });
    }

    private void checkAuthStateAndNavigate() {
        if (!isAdded()) {
            return;
        }

        CheckInitialNavigationUseCase.InitialTarget target = checkInitialNavigationUseCase.invoke();
        NavigationRoute targetRoute = null;

        switch (target) {
            case MAP_SCREEN:
                targetRoute = new NavigationRoute.MapRoute();
                break;
            case LOGIN_SCREEN:
                targetRoute = new NavigationRoute.LoginRoute();
                break;
            case CHECKOUT_SCREEN:
                targetRoute = new NavigationRoute.CheckoutRoute();
                break;
        }

        if (targetRoute != null) {
            appNavigator.navigate(targetRoute);
        }

    }
}