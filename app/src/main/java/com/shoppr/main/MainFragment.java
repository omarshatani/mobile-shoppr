package com.shoppr.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.shoppr.R;
import com.shoppr.databinding.FragmentMainBinding;
import com.shoppr.navigation.BottomNavManager;
import com.shoppr.navigation.MainViewPagerAdapter;

public class MainFragment extends Fragment implements BottomNavManager {

	private FragmentMainBinding binding;
	private MainViewPagerAdapter viewPagerAdapter;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = FragmentMainBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setupViewPager();
	}

	@Override
	public void setBottomNavVisibility(boolean isVisible) {
		if (binding == null) return;

		if (isVisible) {
			showBottomNav();
		} else {
			hideBottomNav();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private void setupViewPager() {
		viewPagerAdapter = new MainViewPagerAdapter(this);
		binding.mainViewPager.setAdapter(viewPagerAdapter);
		binding.mainViewPager.setUserInputEnabled(false);

		// This is the key: when a tab is selected, we find its NavController
		// and connect it to the BottomNavigationView
		binding.bottomNavigation.setOnItemSelectedListener(item -> {
			int position = getPositionForMenuItem(item.getItemId());
			if (binding.mainViewPager.getCurrentItem() != position) {
				binding.mainViewPager.setCurrentItem(position, false);
			}
			return true;
		});

		binding.mainViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				binding.bottomNavigation.getMenu().getItem(position).setChecked(true);
			}
		});
	}

	private int getPositionForMenuItem(int menuItemId) {
		if (menuItemId == R.id.map_nav_graph) return 0;
		if (menuItemId == R.id.request_nav_graph) return 1;
		if (menuItemId == R.id.post_nav_graph) return 2;
		if (menuItemId == R.id.profile_nav_graph) return 3;
		return 0;
	}

	private void showBottomNav() {
		if (binding == null) return;

		binding.bottomNavigation.animate()
				.translationY(0f)
				.setInterpolator(new DecelerateInterpolator(2f))
				.withStartAction(() -> {
					if (binding != null) {
						binding.bottomNavigation.setVisibility(View.VISIBLE);
					}
				});
	}

	private void hideBottomNav() {
		if (binding == null) return;

		binding.bottomNavigation.animate()
				.translationY(binding.bottomNavigation.getHeight())
				.setInterpolator(new AccelerateInterpolator(2f))
				.withEndAction(() -> {
					if (binding != null) {
						binding.bottomNavigation.setVisibility(View.GONE);
					}
				});
	}

}