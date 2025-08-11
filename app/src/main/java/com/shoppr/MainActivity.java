package com.shoppr;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.shoppr.databinding.ActivityMainBinding;
import com.shoppr.navigation.Navigator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
	@Inject
	Navigator navigator;
	private ActivityMainBinding binding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}

	}
}