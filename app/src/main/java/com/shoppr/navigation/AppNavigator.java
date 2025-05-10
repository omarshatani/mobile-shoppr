package com.shoppr.navigation;

import android.util.Log;

import androidx.navigation.NavController;

import com.shoppr.R;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppNavigator implements Navigator {
    private final String TAG = AppNavigator.class.getSimpleName();
    private NavController navController;
    private static final Map<Class<? extends NavigationRoute>, Integer> ROUTES = new HashMap<>();

    static {
        ROUTES.put(NavigationRoute.SplashToLogin.class, R.id.action_splash_to_login);
        ROUTES.put(NavigationRoute.SplashToMap.class, R.id.action_splash_to_map);
        ROUTES.put(NavigationRoute.Login.class, R.id.action_global_navigateToLogin);
        ROUTES.put(NavigationRoute.LoginToMap.class, R.id.action_login_to_map);
        ROUTES.put(NavigationRoute.Checkout.class, R.id.action_global_navigateToCheckout);
        ROUTES.put(NavigationRoute.Map.class, R.id.action_global_navigateToMap);
        ROUTES.put(NavigationRoute.Posts.class, R.id.action_global_navigateToPosts);
        ROUTES.put(NavigationRoute.Requests.class, R.id.action_global_navigateToRequests);
        ROUTES.put(NavigationRoute.Profile.class, R.id.action_global_navigateToProfile);
        ROUTES.put(NavigationRoute.ProfileToLogin.class, R.id.action_profile_to_login);
    }

    @Inject
    public AppNavigator() {}

    public void setNavController(NavController navController) {
        this.navController = navController;
    }

    @Override
    public void navigate(NavigationRoute route) {
        if (navController == null) {
            Log.e(TAG, "NavController not found");
            return;
        }

        if (route == null) {
            Log.e(TAG, "Route is null");
            return;
        }

        final int actionId = ROUTES.getOrDefault(route.getClass(), 0);

        if (actionId == 0) {
            Log.e(TAG, "Route not found: " + route.getClass().getSimpleName());
            return;
        }

        try {
            Log.d(TAG, "Navigating to: " + route.getClass().getSimpleName());
            navController.navigate(actionId);
        } catch (IllegalArgumentException exception) {
            Log.e(TAG, "Navigation failed for actionId " + actionId + ". Destination not found?", exception);
        }

    }

    @Override
    public void goBack() {
        if (navController != null) {
            navController.navigateUp();
        }
    }
}
