package com.shoppr.domain;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.shoppr.model.Event;
import com.shoppr.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GetCurrentUserUseCaseImpl implements GetCurrentUserUseCase {
	private static final String TAG = "GetCurrentUserUCImpl";
	private final ObserveAuthStateUseCase observeAuthStateUseCase; // For raw auth user
	private final CreateUserProfileUseCase createUserProfileUseCase; // For getting/creating full profile

	private final LiveData<User> fullUserProfileLiveData;
	private final MutableLiveData<Event<String>> _profileErrorEvents = new MutableLiveData<>();

	@Inject
	public GetCurrentUserUseCaseImpl(
			ObserveAuthStateUseCase observeAuthStateUseCase,
			CreateUserProfileUseCase createUserProfileUseCase
	) {
		this.observeAuthStateUseCase = observeAuthStateUseCase;
		this.createUserProfileUseCase = createUserProfileUseCase;

		LiveData<User> rawAuthUserObservable = this.observeAuthStateUseCase.getRawAuthUser();

		this.fullUserProfileLiveData = Transformations.switchMap(rawAuthUserObservable, rawUser -> {
			final MutableLiveData<User> profileResultLiveData = new MutableLiveData<>();
			if (rawUser != null) {
				Log.d(TAG, "Raw auth user detected (UID: " + rawUser.getId() + "). Fetching/creating full profile via CreateUserProfileUseCase.");
				createUserProfileUseCase.execute(
						rawUser.getId(),
						rawUser.getName(),
						rawUser.getEmail(),
						null, // photoUrl - CreateUserProfileUseCase's underlying UserRepository handles this.
						new CreateUserProfileUseCase.ProfileCreationCallbacks() {
							@Override
							public void onProfileReadyOrExists(@NonNull User fullUser) {
								Log.d(TAG, "Full profile ready for UID: " + fullUser.getId());
								profileResultLiveData.postValue(fullUser);
							}

							@Override
							public void onProfileCreationError(@NonNull String message) {
								Log.e(TAG, "Error getting/creating full profile via CreateUserProfileUseCase: " + message);
								profileResultLiveData.postValue(null);
								_profileErrorEvents.postValue(new Event<>(message));
							}
						}
				);
			} else {
				Log.d(TAG, "No raw auth user detected. Posting null for full profile.");
				profileResultLiveData.postValue(null);
			}
			return profileResultLiveData;
		});
	}

	@Override
	public LiveData<User> getFullUserProfile() {
		return fullUserProfileLiveData;
	}

	@Override
	public LiveData<Event<String>> getProfileErrorEvents() {
		return _profileErrorEvents;
	}

	@Override
	public void startObserving() {
		Log.d(TAG, "startObserving called. Delegating to observeAuthStateUseCase to ensure it's active.");
		observeAuthStateUseCase.startObserving();
	}

	@Override
	public void stopObserving() {
		Log.d(TAG, "stopObserving called. Delegating to observeAuthStateUseCase.");
		observeAuthStateUseCase.stopObserving();
	}
}