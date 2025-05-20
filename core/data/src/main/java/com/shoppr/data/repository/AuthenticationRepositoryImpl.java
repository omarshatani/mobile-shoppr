package com.shoppr.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.shoppr.data.datasource.FirebaseAuthDataSourceImpl;
import com.shoppr.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthenticationRepositoryImpl implements AuthenticationRepository {
	private static final String TAG = "AuthRepoImpl";
	private final FirebaseAuthDataSourceImpl firebaseAuthDataSource; // Using concrete FirebaseAuthDataSource

	@Inject
	public AuthenticationRepositoryImpl(FirebaseAuthDataSourceImpl firebaseAuthDataSource) {
		this.firebaseAuthDataSource = firebaseAuthDataSource;
	}

	@Override
	public LiveData<User> getRawAuthState() {
		// This LiveData<User> comes from FirebaseAuthDataSourceImpl after mapping FirebaseUser
		return firebaseAuthDataSource.getUserAuthStateLiveData();
	}

	@Override
	public boolean isUserLoggedIn() {
		return firebaseAuthDataSource.getCurrentFirebaseUser() != null;
	}

	@Override
	public void logout() {
		Log.d(TAG, "logout() called. Delegating to FirebaseAuthDataSource.");
		firebaseAuthDataSource.signOut();
	}

	@Override
	public void startObservingAuthState() {
		Log.d(TAG, "startObservingAuthState() called. Delegating to FirebaseAuthDataSource.");
		firebaseAuthDataSource.startObserving();
	}

	@Override
	public void stopObservingAuthState() {
		Log.d(TAG, "stopObservingAuthState() called. Delegating to FirebaseAuthDataSource.");
		firebaseAuthDataSource.stopObserving();
	}
}
