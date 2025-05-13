package com.shoppr.domain;

import android.util.Log;

import androidx.annotation.Nullable;

import com.shoppr.data.repository.UserRepository;
import com.shoppr.model.User;

import javax.inject.Inject;

public class CreateUserProfileUseCaseImpl implements CreateUserProfileUseCase {
	private static final String TAG_CREATE_USER_UC = "CreateUserProfileUC";
	private final UserRepository userRepository;

	@Inject
	public CreateUserProfileUseCaseImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public void execute(String uid, @Nullable String displayName, @Nullable String email, @Nullable String photoUrl,
											ProfileCreationCallbacks callbacks) {
		Log.d(TAG_CREATE_USER_UC, "Executing for UID: " + uid);
		userRepository.getOrCreateUserProfile(uid, displayName, email, photoUrl, new UserRepository.ProfileOperationCallbacks() {
			@Override
			public void onSuccess(User user) {
				callbacks.onProfileReadyOrExists(user);
			}

			@Override
			public void onError(String message) {
				callbacks.onProfileCreationError(message);
			}
		});
	}
}