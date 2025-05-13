package com.shoppr.data.datasource;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shoppr.data.adapter.FirebaseUserMapper;
import com.shoppr.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseAuthDataSourceImpl implements FirebaseAuthDataSource {
	private static final String TAG = "FirebaseAuthDS";
	private final FirebaseAuth firebaseAuth;
	private final FirebaseUserMapper userMapper;
	private final MutableLiveData<User> domainUserLiveData = new MutableLiveData<>(null); // Exposes domain User
	private FirebaseAuth.AuthStateListener authStateListener;

	@Inject
	public FirebaseAuthDataSourceImpl(FirebaseAuth firebaseAuth, FirebaseUserMapper userMapper) {
		this.firebaseAuth = firebaseAuth;
		this.userMapper = userMapper;
		updateDomainUser();
	}

	public LiveData<User> getDomainUserAuthStateLiveData() {
		return domainUserLiveData;
	}

	public FirebaseUser getCurrentFirebaseUser() { // Still useful for some direct Firebase ops if needed
		return firebaseAuth.getCurrentUser();
	}

	public void signOut() {
		firebaseAuth.signOut();
	}

	public void startObserving() {
		if (authStateListener == null) {
			authStateListener = auth -> {
				FirebaseUser fUser = auth.getCurrentUser();
				Log.d(TAG, "Listener triggered. FirebaseUser: " + (fUser != null ? fUser.getUid() : "null"));
				domainUserLiveData.postValue(userMapper.toUser(fUser));
			};
		}
		firebaseAuth.addAuthStateListener(authStateListener);
		Log.d(TAG, "Started observing auth state in FirebaseAuthDataSource.");
	}

	public void stopObserving() {
		if (authStateListener != null) {
			firebaseAuth.removeAuthStateListener(authStateListener);
			Log.d(TAG, "Stopped observing auth state in FirebaseAuthDataSource.");
		}
	}

	private void updateDomainUser() {
		domainUserLiveData.postValue(this.userMapper.toUser(this.firebaseAuth.getCurrentUser()));
	}
}