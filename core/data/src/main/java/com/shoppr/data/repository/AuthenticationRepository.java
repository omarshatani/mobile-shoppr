package com.shoppr.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.shoppr.data.model.IAuthenticationRepository;

import javax.inject.Inject;

public class AuthenticationRepository implements IAuthenticationRepository {
    FirebaseAuth firebaseAuth;

    @Inject
    public AuthenticationRepository(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    @Override
    public void logout() {
        firebaseAuth.signOut();
    }
}
