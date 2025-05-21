package com.shoppr.data.usecase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.domain.CreateUserProfileUseCase;
import com.shoppr.domain.UserRepository;
import com.shoppr.model.User;

import javax.inject.Inject;

public class CreateUserProfileUseCaseImpl implements CreateUserProfileUseCase {
	private static final String TAG = "CreateUserProfileUC";
	private final UserRepository userRepository;

	@Inject
	public CreateUserProfileUseCaseImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public void execute(String uid, @Nullable String displayName, @Nullable String email, @Nullable String photoUrl,
											ProfileCreationCallbacks callbacks) {
		Log.d(TAG, "Executing for UID: " + uid);
		userRepository.getOrCreateUserProfile(uid, displayName, email, photoUrl, new UserRepository.ProfileOperationCallbacks() {
			@Override
			public void onSuccess(@NonNull User user) {
				callbacks.onProfileReadyOrExists(user);
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onProfileCreationError(message);
			}
		});
	}
}