package com.shoppr.navigation;

import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

public interface Navigator {
	void setNavController(NavController navController);

	void navigate(NavigationRoute route);

	void navigate(NavDirections directions);

	void goBack();
}