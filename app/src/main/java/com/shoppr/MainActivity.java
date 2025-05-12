package com.shoppr;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.shoppr.navigation.AppNavigator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
	@Inject
	AppNavigator appNavigator;
	private BottomNavigationView bottomNavView;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		// Edge-to-edge
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}

		// Find the BottomNavigationView
		bottomNavView = findViewById(R.id.bottom_navigation_view);

		NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

		if (navHostFragment == null) {
			Log.e("MainActivity", "Cannot inject navController: NavHostFragment is null");
			return;
		}

		NavController navController = navHostFragment.getNavController();
		appNavigator.setNavController(navController);

		NavigationUI.setupWithNavController(bottomNavView, navController);
		// Setup listener to hide/show BottomNav based on destination
		setupBottomNavVisibility(navController);
	}

	// Hides/shows the BottomNavigationView based on the current destination
	private void setupBottomNavVisibility(NavController navController) {
		navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
			int destinationId = destination.getId();
			// Define which destinations should HIDE the bottom nav
			boolean shouldHide = destinationId == R.id.splashFragment ||
					isLoginFlowDestination(destination); // Check if in login flow

			if (shouldHide) {
				bottomNavView.setVisibility(View.GONE);
			} else {
				bottomNavView.setVisibility(View.VISIBLE);
			}
		});
	}

	// Helper to check if the current destination is part of the login flow graph
	private boolean isLoginFlowDestination(NavDestination destination) {
		NavGraph currentGraph = destination.getParent();
		// Check if the destination's ID or its parent graph's ID matches the login graph ID
		// Ensure R.id.login_nav_graph is the correct ID from main_nav_graph.xml
		return destination.getId() == R.id.login_nav_graph ||
				(currentGraph != null && currentGraph.getId() == R.id.login_nav_graph);
		// Refine this check if login flow has nested fragments you also want to hide the bar for
	}

}
