package com.shoppr.data.repository;

import androidx.annotation.Nullable;

import com.shoppr.model.User;

public interface UserRepository { // Renamed from IUserRepository
	interface ProfileOperationCallbacks {
		void onSuccess(User user); // Returns the full, created/fetched domain User from Firestore

		void onError(String message);
	}

	void getOrCreateUserProfile(String uid, @Nullable String displayName, @Nullable String email, @Nullable String photoUrl, ProfileOperationCallbacks callbacks);
}