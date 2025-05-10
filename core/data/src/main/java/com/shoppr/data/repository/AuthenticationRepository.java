package com.shoppr.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.shoppr.data.datasource.FirebaseAuthDataSource;
import com.shoppr.data.model.IAuthenticationRepository;

import javax.inject.Inject;

public class AuthenticationRepository implements IAuthenticationRepository {
    FirebaseAuthDataSource firebaseAuthDataSource;

    @Inject
    public AuthenticationRepository(FirebaseAuthDataSource firebaseAuthDataSource) {
        this.firebaseAuthDataSource = firebaseAuthDataSource;
    }

    @Override
    public boolean isUserLoggedIn() {
        return firebaseAuthDataSource.getCurrentUser() != null;
    }

    @Override
    public void logout() {
        firebaseAuthDataSource.signOut();
    }
}
