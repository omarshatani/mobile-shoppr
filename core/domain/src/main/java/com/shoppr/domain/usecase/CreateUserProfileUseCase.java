package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.shoppr.model.User;

public interface CreateUserProfileUseCase {

	interface ProfileOperationCallbacks {
		void onSuccess(@NonNull User user);

		void onError(@NonNull String message);
	}

	void execute(
			@NonNull String uid,
			@Nullable String displayName,
			@Nullable String email,
			@Nullable String photoUrl,
			@NonNull ProfileOperationCallbacks callbacks
	);
}