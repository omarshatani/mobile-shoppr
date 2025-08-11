package com.shoppr.navigation;

public abstract class NavigationRoute {

	private NavigationRoute() {
	}

	public static final class SplashToLogin extends NavigationRoute {
	}

	public static final class SplashToMain extends NavigationRoute {
	}

	public static final class Login extends NavigationRoute {
	}

	public static final class LoginToMap extends NavigationRoute {
	}


	public static final class Map extends NavigationRoute {
	}

	public static final class CreateNewPost extends NavigationRoute {
	}

	public static final class ProfileToLogin extends NavigationRoute {
	}

	public static final class Favorites extends NavigationRoute {
	}

}
