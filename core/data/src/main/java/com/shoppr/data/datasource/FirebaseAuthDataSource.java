package com.shoppr.data.datasource;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseUser;
import com.shoppr.model.User;

public interface FirebaseAuthDataSource {
	LiveData<User> getDomainUserAuthStateLiveData();
	FirebaseUser getCurrentFirebaseUser();
	void startObserving();
	void stopObserving();
	void signOut();
}
