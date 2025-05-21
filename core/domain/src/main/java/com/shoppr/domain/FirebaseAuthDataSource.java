package com.shoppr.domain;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseUser;
import com.shoppr.model.User;

public interface FirebaseAuthDataSource {
	LiveData<User> getUserAuthStateLiveData();
	FirebaseUser getCurrentFirebaseUser();
	void startObserving();
	void stopObserving();
	void signOut();
}
