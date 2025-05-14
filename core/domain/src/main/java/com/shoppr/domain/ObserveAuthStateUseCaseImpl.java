package com.shoppr.domain;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.shoppr.data.repository.AuthenticationRepository;
import com.shoppr.data.repository.UserRepository;
import com.shoppr.model.Event;
import com.shoppr.model.User;

import javax.inject.Inject;

public class ObserveAuthStateUseCaseImpl implements ObserveAuthStateUseCase {
	private static final String TAG_OBSERVE_UC = "ObserveAuthUCImpl";
	private final AuthenticationRepository authRepository;
	private final UserRepository userRepository;
	private final MutableLiveData<Event<String>> _authenticationErrorEvents = new MutableLiveData<>();
	private final LiveData<User> finalLoggedInUserWithProfileLiveData;

	@Inject
	public ObserveAuthStateUseCaseImpl(AuthenticationRepository authRepository, UserRepository userRepository) {
		this.authRepository = authRepository;
		this.userRepository = userRepository;
		LiveData<User> basicAuthUserLiveData = authRepository.getRawAuthState();

		this.finalLoggedInUserWithProfileLiveData = Transformations.switchMap(basicAuthUserLiveData, basicAuthUser -> {
			MutableLiveData<User> fullProfileUserLiveData = new MutableLiveData<>();
			if (basicAuthUser != null) {
				Log.d(TAG_OBSERVE_UC, "SWITCHMAP: basicAuthUser is NOT NULL (UID: " + basicAuthUser.getId() + "). Getting/creating full profile.");
				userRepository.getOrCreateUserProfile(
						basicAuthUser.getId(), basicAuthUser.getName(), basicAuthUser.getEmail(), null,
						new UserRepository.ProfileOperationCallbacks() {
							@Override
							public void onSuccess(User fullUserProfile) {
								Log.d(TAG_OBSERVE_UC, "Profile op success. Full profile for: " + fullUserProfile.getId());
								fullProfileUserLiveData.postValue(fullUserProfile);
							}

							@Override
							public void onError(String message) {
								Log.e(TAG_OBSERVE_UC, "Profile op error: " + message);
								fullProfileUserLiveData.postValue(null);
								_authenticationErrorEvents.postValue(new Event<>(message));
							}
						});
			} else {
				Log.d(TAG_OBSERVE_UC, "SWITCHMAP: basicAuthUser IS NULL. Logout detected. Emitting null for loggedInUser.");
				fullProfileUserLiveData.postValue(null);
			}
			return fullProfileUserLiveData;
		});
	}

	@Override
	public LiveData<User> getLoggedInUserWithProfile() {
		return finalLoggedInUserWithProfileLiveData;
	}

	@Override
	public LiveData<Event<String>> getAuthenticationErrorEvents() {
		return _authenticationErrorEvents;
	}

	@Override
	public void startObserving() {
		authRepository.startObservingAuthState();
	}

	@Override
	public void stopObserving() {
		authRepository.stopObservingAuthState();
	}
}