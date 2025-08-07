package com.shoppr.navigation;

import androidx.navigation.NavController;

public interface Navigator {
	void setNavController(NavController navController);
    void navigate(NavigationRoute route);
    void goBack();
}