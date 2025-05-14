package com.shoppr.domain;

import androidx.annotation.Nullable;

import com.shoppr.model.User;

public interface CreateUserProfileUseCase {
	interface ProfileCreationCallbacks {
		void onProfileReadyOrExists(User user); // Returns the full User domain object

		void onProfileCreationError(String message);
	}

	void execute(String uid, @Nullable String displayName, @Nullable String email, @Nullable String photoUrl, ProfileCreationCallbacks callbacks);
}