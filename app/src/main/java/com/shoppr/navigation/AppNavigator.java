package com.shoppr.navigation;

import android.util.Log;

import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

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
        // --- THIS MAP IS NOW UPDATED AND CORRECTED ---

        // Splash Screen Navigation
        ROUTES.put(NavigationRoute.SplashToLogin.class, R.id.action_splash_to_login);
        ROUTES.put(NavigationRoute.SplashToMain.class, R.id.action_splash_to_main);

        // Login Screen Navigation
        ROUTES.put(NavigationRoute.LoginToMap.class, R.id.action_login_to_main);

        // Global Navigation Actions
        ROUTES.put(NavigationRoute.Login.class, R.id.action_global_navigate_to_login);
        ROUTES.put(NavigationRoute.ProfileToLogin.class, R.id.action_global_navigate_to_login);
        ROUTES.put(NavigationRoute.CreateNewPost.class, com.shoppr.post.R.id.action_post_to_create_post);
        ROUTES.put(NavigationRoute.Favorites.class, com.shoppr.profile.R.id.action_profile_to_favorites);

        // Note: Direct navigation to main tabs (Map, Posts, etc.) is now handled by the ViewPager2
        // So, we don't need dedicated global actions for them unless for a specific purpose like a notification.
    }

    @Inject
    public AppNavigator() {}

    @Override
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
            Log.e(TAG, "Route not found in AppNavigator map for: " + route.getClass().getSimpleName());
            return;
        }

        try {
            Log.d(TAG, "Navigating with action ID for route: " + route.getClass().getSimpleName());
            navController.navigate(actionId);
        } catch (IllegalArgumentException exception) {
            Log.e(TAG, "Navigation failed for actionId " + actionId + ". Destination not found from current location?", exception);
        }
    }

    @Override
    public void navigate(NavDirections directions) {
        if (navController == null) {
            Log.e(TAG, "NavController not found");
            return;
        }

        if (directions == null) {
            Log.e(TAG, "Directions is null");
            return;
        }

        try {
            Log.d(TAG, "Navigating with action ID for route: " + directions.getClass().getSimpleName());
            navController.navigate(directions);
        } catch (IllegalArgumentException exception) {
            Log.e(TAG, "Navigation failed for actionId " + directions + ". Destination not found from current location?", exception);
        }
    }

    @Override
    public void goBack() {
        if (navController != null) {
            navController.popBackStack();
        }
    }
}