package com.shoppr.navigation;

public interface NavigationRoute {
	// Sealed interface is a modern Java feature for creating restricted class hierarchies.
	// It's like an advanced enum.

	// --- Initial App Flow ---
	// From Splash screen to the two possible starting points.
	final class SplashToLogin implements NavigationRoute {
	}

	final class SplashToMap implements NavigationRoute {
	} // Note: In our refactor, this now goes to MainFragment

	// --- Login Flow ---
	// From the Login screen to the main part of the app.
	final class LoginToMap implements NavigationRoute {
	} // This also goes to MainFragment

	// --- Global Actions from Anywhere ---
	// These routes correspond to global actions in your main_nav_graph.
	// They can be called from any screen to navigate to a top-level destination.

	final class ProfileToLogin implements NavigationRoute {
	} // For handling logout

	final class CreatePost implements NavigationRoute {
	} // To open the create post screen

	final class CreatePostToMap implements NavigationRoute {
	} // To return to the map screen

	final class ProfileToFavorites implements NavigationRoute {
	}

}