package com.shoppr.data.usecase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.shoppr.domain.usecase.CreateUserProfileUseCase;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.ObserveAuthStateUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.User;

import javax.inject.Inject;

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

        // Get the LiveData for the raw authenticated user (basic info from auth provider)
        LiveData<User> rawAuthUserObservable = this.observeAuthStateUseCase.getRawAuthUser();

        // Transform the raw auth user LiveData. When it emits a user (basic info),
        // we then use CreateUserProfileUseCase to fetch/create their full profile from Firestore.
        this.fullUserProfileLiveData = Transformations.switchMap(rawAuthUserObservable, rawUser -> {
            final MutableLiveData<User> profileResultLiveData = new MutableLiveData<>();
            if (rawUser != null) {
                Log.d(TAG, "Raw auth user detected (UID: " + rawUser.getId() + "). Fetching/creating full profile via CreateUserProfileUseCase.");
                createUserProfileUseCase.execute(
                        rawUser.getId(),
                        rawUser.getName(),    // Name from the basic User (originally from FirebaseUser)
                        rawUser.getEmail(),   // Email from the basic User
                        null, // photoUrl - CreateUserProfileUseCase's underlying UserRepository handles this.
                        // The rawUser from ObserveAuthStateUseCase might not have photoUrl if your mapper doesn't include it.
                        new CreateUserProfileUseCase.ProfileCreationCallbacks() {
                            @Override
                            public void onProfileReadyOrExists(@NonNull User fullUser) {
                                Log.d(TAG, "Full profile ready for UID: " + fullUser.getId() + ". Has location: " + (fullUser.getLastLatitude() != null));
                                profileResultLiveData.postValue(fullUser);
                            }

                            @Override
                            public void onProfileCreationError(@NonNull String message) {
                                Log.e(TAG, "Error getting/creating full profile via CreateUserProfileUseCase: " + message);
                                profileResultLiveData.postValue(null); // Signal error by posting null for the user
                                _profileErrorEvents.postValue(new Event<>(message));
                            }
                        }
                );
            } else {
                Log.d(TAG, "No raw auth user detected (logout or initial state). Posting null for full profile.");
                profileResultLiveData.postValue(null); // No raw user, so no full profile
            }
            return profileResultLiveData; // This is the LiveData<User> that getFullUserProfile() will return
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
        // This use case relies on ObserveAuthStateUseCase to be started.
        // Calling startObserving on the dependency ensures the upstream data flow is active.
        Log.d(TAG, "startObserving called. Delegating to observeAuthStateUseCase.startObserving().");
        observeAuthStateUseCase.startObserving();
    }

    @Override
    public void stopObserving() {
        // Similarly, stop the upstream observation when this use case is no longer needed.
        Log.d(TAG, "stopObserving called. Delegating to observeAuthStateUseCase.stopObserving().");
        observeAuthStateUseCase.stopObserving();
    }
}