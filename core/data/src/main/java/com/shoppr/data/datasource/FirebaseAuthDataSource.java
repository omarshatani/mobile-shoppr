package com.shoppr.data.datasource;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

public class FirebaseAuthDataSource {
	FirebaseAuth auth;

	@Inject
	public FirebaseAuthDataSource(FirebaseAuth auth) {
		this.auth = auth;
	}

	public FirebaseUser getCurrentUser() {
		return auth.getCurrentUser();
	}

	public void signOut() {
		auth.signOut();
	}
}
