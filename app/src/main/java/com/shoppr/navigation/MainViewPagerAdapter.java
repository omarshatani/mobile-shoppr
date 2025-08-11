package com.shoppr.navigation;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainViewPagerAdapter extends FragmentStateAdapter {

	private static final int NUM_TABS = 4;

	public MainViewPagerAdapter(@NonNull Fragment fragment) {
		super(fragment);
	}

	@NonNull
	@Override
	public Fragment createFragment(int position) {
		// For each tab, create a new NavHostFragment and set its graph
		switch (position) {
			case 0:
				return NavHostFragment.create(com.shoppr.map.R.navigation.map_nav_graph);
			case 1:
				return NavHostFragment.create(com.shoppr.request.R.navigation.request_nav_graph);
			case 2:
				return NavHostFragment.create(com.shoppr.post.R.navigation.post_nav_graph);
			case 3:
				return NavHostFragment.create(com.shoppr.profile.R.navigation.profile_nav_graph);
			default:
				throw new IllegalStateException("Invalid position: " + position);
		}
	}

	@Override
	public int getItemCount() {
		return NUM_TABS;
	}
}