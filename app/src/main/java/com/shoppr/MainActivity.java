package com.shoppr;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.shoppr.navigation.AppNavigator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
	@Inject
	AppNavigator appNavigator;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}

		NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

		if (navHostFragment == null) {
			Log.e("MainActivity", "Cannot inject navController: NavHostFragment is null");
			return;
		}

		NavController navController = navHostFragment.getNavController();
		appNavigator.setNavController(navController);

	}

}
