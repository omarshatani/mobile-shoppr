package com.shoppr.domain;

import androidx.annotation.Nullable;

import com.shoppr.model.User;

public interface CreateUserProfileUseCase {
	interface ProfileCreationCallbacks { // Remains the same
		void onProfileReadyOrExists(User user); // Pass the domain user

		void onProfileCreationError(String message);
	}

	void execute(String uid, @Nullable String displayName, @Nullable String email, @Nullable String photoUrl, ProfileCreationCallbacks callbacks);
}
