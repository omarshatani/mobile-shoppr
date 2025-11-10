package com.shoppr.navigation;

import android.os.Bundle;
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
        // Initial App Flow
        ROUTES.put(NavigationRoute.SplashToLogin.class, R.id.action_splash_to_login);
        ROUTES.put(NavigationRoute.SplashToMap.class, R.id.action_splash_to_main);
        ROUTES.put(NavigationRoute.LoginToMap.class, R.id.action_login_to_main);

        // Global Actions from anywhere in the app
        ROUTES.put(NavigationRoute.CreatePost.class, R.id.action_global_navigate_to_create_post);
        ROUTES.put(NavigationRoute.CreatePostToMap.class, R.id.action_global_create_post_to_map);
        ROUTES.put(NavigationRoute.ProfileToLogin.class, R.id.action_global_navigate_to_login);
        ROUTES.put(NavigationRoute.RequestToCheckout.class, R.id.action_global_request_to_checkout);
        ROUTES.put(NavigationRoute.Request.class, R.id.action_global_navigate_to_request);
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
            Log.e(TAG, "NavController not found in AppNavigator.");
            return;
        }

        if (route == null) {
            Log.e(TAG, "NavigationRoute is null.");
            return;
        }

        final int actionId = ROUTES.getOrDefault(route.getClass(), 0);

        if (actionId == 0) {
            Log.e(TAG, "Route not found in AppNavigator map for: " + route.getClass().getSimpleName());
            return;
        }

        try {
            Log.d(TAG, "Executing global navigation for route: " + route.getClass().getSimpleName());
            navController.navigate(actionId);
        } catch (IllegalArgumentException exception) {
            Log.e(TAG, "Global navigation failed for actionId " + actionId + ". Is the action defined in main_nav_graph.xml?", exception);
        }
    }

    @Override
    public void navigate(NavigationRoute route, Bundle args) {
        if (navController == null) {
            Log.e(TAG, "NavController not found in AppNavigator.");
            return;
        }

        if (route == null) {
            Log.e(TAG, "NavigationRoute is null.");
            return;
        }

        final int actionId = ROUTES.getOrDefault(route.getClass(), 0);

        if (actionId == 0) {
            Log.e(TAG, "Route not found in AppNavigator map for: " + route.getClass().getSimpleName());
            return;
        }

        try {
            Log.d(TAG, "Executing global navigation for route: " + route.getClass().getSimpleName());
            navController.navigate(actionId, args);
        } catch (IllegalArgumentException exception) {
            Log.e(TAG, "Global navigation failed for actionId " + actionId + ". Is the action defined in main_nav_graph.xml?", exception);
        }
    }

    @Override
    public void goBack() {
        if (navController != null) {
            navController.popBackStack();
        }
    }
}