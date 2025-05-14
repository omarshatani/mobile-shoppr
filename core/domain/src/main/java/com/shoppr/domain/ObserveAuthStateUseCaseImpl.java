package com.shoppr.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.shoppr.data.repository.AuthenticationRepository;
import com.shoppr.model.User;

import javax.inject.Inject;

public class ObserveAuthStateUseCaseImpl implements ObserveAuthStateUseCase {
	private final AuthenticationRepository authRepository;
	private final LiveData<User> finalLoggedInUserWithProfileLiveData;
	private AuthCallbacks authCallbacks;

	@Inject
	public ObserveAuthStateUseCaseImpl(AuthenticationRepository authRepository, CreateUserProfileUseCase createUserProfileUseCase) {
		this.authRepository = authRepository;
		LiveData<User> basicAuthUserLiveData = authRepository.getAuthState();
		this.finalLoggedInUserWithProfileLiveData = Transformations.switchMap(basicAuthUserLiveData, basicAuthUser -> {
			MutableLiveData<User> fullProfileUserLiveData = new MutableLiveData<>();
			if (basicAuthUser != null) {
				createUserProfileUseCase.execute(
						basicAuthUser.getId(), basicAuthUser.getName(), basicAuthUser.getEmail(), null,
						new CreateUserProfileUseCase.ProfileCreationCallbacks() {
							@Override
							public void onProfileReadyOrExists(User fullUserProfile) {
								fullProfileUserLiveData.postValue(fullUserProfile);
								if (authCallbacks != null)
									authCallbacks.onUserAuthenticatedAndProfileReady(fullUserProfile);
							}

							@Override
							public void onProfileCreationError(String message) {
								fullProfileUserLiveData.postValue(null);
								if (authCallbacks != null) authCallbacks.onAuthenticationError(message);
							}
						});
			} else {
				fullProfileUserLiveData.postValue(null);
				if (authCallbacks != null) {
					authCallbacks.onUserLoggedOut();
				}
			}
			return fullProfileUserLiveData;
		});
	}

	@Override
	public LiveData<User> getLoggedInUser() {
		return finalLoggedInUserWithProfileLiveData;
	}

	@Override
	public void startObserving() {
		authRepository.startObservingAuthState();
	}

	@Override
	public void stopObserving() {
		authRepository.stopObservingAuthState();
	}

	@Override
	public void setAuthCallbacks(AuthCallbacks callbacks) {
		this.authCallbacks = callbacks;
	}
}