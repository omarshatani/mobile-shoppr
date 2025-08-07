package com.shoppr.data.datasource;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shoppr.data.adapter.FirebaseUserMapper;
import com.shoppr.domain.datasource.FirebaseAuthDataSource;
import com.shoppr.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseAuthDataSourceImpl implements FirebaseAuthDataSource {
	private static final String TAG = "FirebaseAuthDSImpl";
	private final FirebaseAuth firebaseAuthSdk; // The actual Firebase SDK
	private final FirebaseUserMapper userMapper; // Mapper to convert to domain User

	private final MutableLiveData<User> domainUserAuthStateLiveData = new MutableLiveData<>(null);
	private FirebaseAuth.AuthStateListener authStateListener;

	@Inject
	public FirebaseAuthDataSourceImpl(FirebaseAuth firebaseAuthSdk, FirebaseUserMapper userMapper) {
		this.firebaseAuthSdk = firebaseAuthSdk;
		this.userMapper = userMapper;
		// Initialize LiveData with the current state
		FirebaseUser initialFirebaseUser = this.firebaseAuthSdk.getCurrentUser();
		Log.d(TAG, "Initializing with FirebaseUser: " + (initialFirebaseUser != null ? initialFirebaseUser.getUid() : "null"));
		domainUserAuthStateLiveData.postValue(this.userMapper.toUser(initialFirebaseUser));
	}

	@Override
	public LiveData<User> getDomainUserAuthStateLiveData() {
		return domainUserAuthStateLiveData;
	}

	@Override
	public boolean isCurrentUserLoggedIn() {
		return firebaseAuthSdk.getCurrentUser() != null;
	}

	@Override
	public void signOut() {
		Log.d(TAG, "Signing out user from Firebase.");
		firebaseAuthSdk.signOut();
		// Listener will update domainUserAuthStateLiveData
	}

	@Override
	public void startObserving() {
		if (authStateListener == null) {
			authStateListener = authSdk -> {
				FirebaseUser fUser = authSdk.getCurrentUser();
				Log.d(TAG, "Firebase AuthStateListener triggered. FirebaseUser: " + (fUser != null ? fUser.getUid() : "null"));
				domainUserAuthStateLiveData.postValue(userMapper.toUser(fUser));
			};
		}
		firebaseAuthSdk.addAuthStateListener(authStateListener);
		Log.d(TAG, "Started observing auth state in FirebaseAuthDataSourceImpl.");
	}

	@Override
	public void stopObserving() {
		if (authStateListener != null) {
			firebaseAuthSdk.removeAuthStateListener(authStateListener);
			Log.d(TAG, "Stopped observing auth state in FirebaseAuthDataSourceImpl.");
		}
	}
}