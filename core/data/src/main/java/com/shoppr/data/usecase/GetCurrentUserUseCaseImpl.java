package com.shoppr.data.usecase;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.shoppr.domain.datasource.FirebaseAuthDataSource;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.model.Event;
import com.shoppr.model.User;

import javax.inject.Inject;

public class GetCurrentUserUseCaseImpl implements GetCurrentUserUseCase {

    private static final String TAG = "GetCurrentUserUseCase";
    private final FirebaseAuthDataSource firebaseAuthDataSource;
    private final FirebaseFirestore firestore;
    private final MutableLiveData<Event<String>> _profileErrorEvents = new MutableLiveData<>();
    private final LiveData<User> _fullUserProfile;
    private ListenerRegistration userProfileListenerRegistration;

    @Inject
    public GetCurrentUserUseCaseImpl(FirebaseAuthDataSource firebaseAuthDataSource, FirebaseFirestore firestore) {
        this.firebaseAuthDataSource = firebaseAuthDataSource;
        this.firestore = firestore;

        _fullUserProfile = Transformations.switchMap(firebaseAuthDataSource.getDomainUserAuthStateLiveData(), authUser -> {
            Log.d(TAG, "Auth state changed. User: " + (authUser != null ? authUser.getId() : "null"));

            if (userProfileListenerRegistration != null) {
                userProfileListenerRegistration.remove();
            }

            if (authUser == null || authUser.getId() == null) {
                Log.d(TAG, "User is logged out. Returning LiveData with null.");
                return new MutableLiveData<>(null);
            }

            Log.d(TAG, "User is logged in. Attaching Firestore listener for UID: " + authUser.getId());
            MutableLiveData<User> firestoreUserLiveData = new MutableLiveData<>();
            userProfileListenerRegistration = firestore.collection("users").document(authUser.getId())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Firestore listener error for UID: " + authUser.getId(), e);
                        _profileErrorEvents.postValue(new Event<>("Error listening to user profile: " + e.getMessage()));
                        firestoreUserLiveData.postValue(null);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        User fullUserProfile = snapshot.toObject(User.class);
                        if (fullUserProfile != null) {
                            fullUserProfile.setId(snapshot.getId());
                            Log.i(TAG, "Firestore listener received update for UID: " + fullUserProfile.getId() + ". Favorites count: " + (fullUserProfile.getFavoritePosts() != null ? fullUserProfile.getFavoritePosts().size() : "null"));
                        }
                        firestoreUserLiveData.postValue(fullUserProfile);
                    } else {
                        Log.w(TAG, "User document does not exist for UID: " + authUser.getId());
                        firestoreUserLiveData.postValue(authUser);
                    }
                });
            return firestoreUserLiveData;
        });
    }

    @Override
    public LiveData<User> getFullUserProfile() {
        return _fullUserProfile;
    }

    @Override
    public LiveData<Event<String>> getProfileErrorEvents() {
        return _profileErrorEvents;
    }

    @Override
    public void startObserving() {
        firebaseAuthDataSource.startObserving();
    }

    @Override
    public void stopObserving() {
        firebaseAuthDataSource.stopObserving();
        if (userProfileListenerRegistration != null) {
            userProfileListenerRegistration.remove();
            userProfileListenerRegistration = null;
        }
    }
}