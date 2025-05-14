package com.shoppr.data.repository;

import androidx.lifecycle.LiveData;

import com.shoppr.data.datasource.FirebaseAuthDataSourceImpl;
import com.shoppr.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthenticationRepositoryImpl implements AuthenticationRepository {
	private final FirebaseAuthDataSourceImpl firebaseAuthDataSourceImpl;

	@Inject
	public AuthenticationRepositoryImpl(FirebaseAuthDataSourceImpl firebaseAuthDataSourceImpl) {
		this.firebaseAuthDataSourceImpl = firebaseAuthDataSourceImpl;
	}

	@Override
	public LiveData<User> getRawAuthState() {
		return firebaseAuthDataSourceImpl.getUserAuthStateLiveData();
	}

	@Override
	public boolean isUserLoggedIn() {
		return firebaseAuthDataSourceImpl.getCurrentFirebaseUser() != null;
	}

	@Override
	public void logout() {
		firebaseAuthDataSourceImpl.signOut();
	}

	@Override
	public void startObservingAuthState() {
		firebaseAuthDataSourceImpl.startObserving();
	}

	@Override
	public void stopObservingAuthState() {
		firebaseAuthDataSourceImpl.stopObserving();
	}
}
