package com.shoppr.data.adapter;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.shoppr.domain.FirebaseUserMapper;
import com.shoppr.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseUserToUserMapper implements FirebaseUserMapper {
	@Inject
	public FirebaseUserToUserMapper() {
	}

	@Override
	@Nullable
	public User toUser(@Nullable FirebaseUser firebaseUser) {
		if (firebaseUser == null) return null;
		return new User.Builder()
				.id(firebaseUser.getUid())
				.name(firebaseUser.getDisplayName())
				.email(firebaseUser.getEmail())
				// phoneNumber and address are not in FirebaseUser.
				// They will be populated when fetching the full profile from Firestore.
				.build();
	}
}