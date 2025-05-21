package com.shoppr.data.adapter;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.shoppr.model.User;


public interface FirebaseUserMapper {
	@Nullable
	User toUser (@Nullable FirebaseUser firebaseUser);
}
