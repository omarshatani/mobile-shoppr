package com.shoppr.data.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthDataSource {
	final FirebaseAuth auth = FirebaseAuth.getInstance();

	public FirebaseUser getCurrentUser() {
		return auth.getCurrentUser();
	}

	public void signIn() {
		return;
	}
}
