package com.shoppr.data.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shoppr.domain.repository.UserRepository;
import com.shoppr.domain.usecase.UpdateUserDefaultLocationUseCase;

import javax.inject.Inject;

public class UpdateUserDefaultLocationUseCaseImpl implements UpdateUserDefaultLocationUseCase {

    private final UserRepository userRepository;

    @Inject
    public UpdateUserDefaultLocationUseCaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
		public void execute(
				double latitude,
				double longitude,
				@Nullable String addressName,
				@NonNull UpdateLocationCallbacks callbacks
		) {
			userRepository.updateUserDefaultLocation(latitude, longitude, addressName, new UserRepository.OperationCallbacks() {
            @Override
            public void onSuccess() {
                callbacks.onLocationUpdateSuccess();
            }

            @Override
            public void onError(@NonNull String message) {
                callbacks.onLocationUpdateError(message);
            }
        });
    }
}