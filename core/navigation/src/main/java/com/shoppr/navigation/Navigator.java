package com.shoppr.navigation;

import android.os.Bundle;

import androidx.navigation.NavController;

public interface Navigator {
	void setNavController(NavController navController);

	void navigate(NavigationRoute route);

	void navigate(NavigationRoute route, Bundle args);

	void goBack();
}