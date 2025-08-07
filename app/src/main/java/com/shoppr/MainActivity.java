package com.shoppr;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.shoppr.databinding.ActivityMainBinding;
import com.shoppr.navigation.NavigationRoute;
import com.shoppr.navigation.Navigator;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity";
	private ActivityMainBinding binding;
	private MainViewModel viewModel;

	@Inject
	Navigator navigator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		viewModel = new ViewModelProvider(this).get(MainViewModel.class);

		setupBottomNavigation();
		observeViewModel();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private void setupBottomNavigation() {
		NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
				.findFragmentById(R.id.nav_host_fragment);
		NavController navController = Objects.requireNonNull(navHostFragment).getNavController();

		navigator.setNavController(navController);

		NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

		navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
			if (destination.getId() == R.id.login_nav_graph || destination.getId() == com.shoppr.post.R.id.create_post_fragment) {
				binding.bottomNavigation.setVisibility(View.GONE);
			} else {
				binding.bottomNavigation.setVisibility(View.VISIBLE);
			}
		});
	}

	private void observeViewModel() {
		viewModel.getInitialNavigationCommand().observe(this, event -> {
			NavigationRoute route = event.getContentIfNotHandled();
			if (route != null) {
				Log.d(TAG, "Handling initial navigation to: " + route.getClass().getSimpleName());
				navigator.navigate(route);
			}
		});
	}

	@Override
	public boolean onSupportNavigateUp() {
		navigator.goBack();
		return true;
	}
}