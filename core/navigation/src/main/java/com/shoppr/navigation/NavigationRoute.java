package com.shoppr.navigation;

public abstract class NavigationRoute {

	private NavigationRoute() {
	}

	public static final class SplashToLogin extends NavigationRoute {
	}

	public static final class SplashToMap extends NavigationRoute {
	}

	public static final class Login extends NavigationRoute {
	}

	public static final class LoginToMap extends NavigationRoute {
	}

	public static final class Checkout extends NavigationRoute {
	}

	public static final class Map extends NavigationRoute {
	}

	public static final class Posts extends NavigationRoute {
	}

	public static final class Requests extends NavigationRoute {
	}

	public static final class Profile extends NavigationRoute {
	}

	public static final class ProfileToLogin extends NavigationRoute {
	}

}
