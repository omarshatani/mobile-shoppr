package com.shoppr.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import com.shoppr.domain.datasource.FirebaseAuthDataSource;
import com.shoppr.domain.repository.AuthenticationRepository;
import com.shoppr.model.User;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthenticationRepositoryImpl implements AuthenticationRepository {
	private static final String TAG = "AuthenticationRepositoryImpl";
	private final FirebaseAuthDataSource firebaseAuthDataSource;

	@Inject
	public AuthenticationRepositoryImpl(FirebaseAuthDataSource firebaseAuthDataSource) {
		this.firebaseAuthDataSource = firebaseAuthDataSource;
	}

	@Override
	public LiveData<User> getAuthState() {
		return firebaseAuthDataSource.getDomainUserAuthStateLiveData();
	}

	@Override
	public boolean isUserLoggedIn() {
		return firebaseAuthDataSource.isCurrentUserLoggedIn();
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