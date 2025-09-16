package com.shoppr.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.shoppr.domain.datasource.FirebaseAuthDataSource;
import com.shoppr.domain.datasource.FirestoreUserDataSource;
import com.shoppr.domain.repository.UserRepository;
import com.shoppr.model.User;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepositoryImpl implements UserRepository {
	private static final String TAG = "UserRepositoryImpl";

	private final FirestoreUserDataSource firestoreUserDataSource;
	private final FirebaseAuthDataSource firebaseAuthDataSource;
	private final LiveData<User> fullUserProfile;
	private ListenerRegistration userProfileListenerRegistration;

	@Inject
	public UserRepositoryImpl(
			FirestoreUserDataSource firestoreUserDataSource,
			FirebaseAuthDataSource firebaseAuthDataSource
	) {
		this.firestoreUserDataSource = firestoreUserDataSource;
		this.firebaseAuthDataSource = firebaseAuthDataSource;

		this.fullUserProfile = Transformations.switchMap(firebaseAuthDataSource.getDomainUserAuthStateLiveData(), authUser -> {
			if (userProfileListenerRegistration != null) {
				userProfileListenerRegistration.remove();
			}

			if (authUser == null || authUser.getId() == null) {
				return new MutableLiveData<>(null);
			}

			MutableLiveData<User> firestoreUserLiveData = new MutableLiveData<>();
			userProfileListenerRegistration = FirebaseFirestore.getInstance().collection("users").document(authUser.getId())
					.addSnapshotListener((snapshot, e) -> {
						if (e != null) {
							Log.e(TAG, "Error listening to user profile", e);
							firestoreUserLiveData.postValue(null);
							return;
						}

						if (snapshot != null && snapshot.exists()) {
							User user = snapshot.toObject(User.class);
							if (user != null) {
								user.setId(snapshot.getId());
							}
							firestoreUserLiveData.postValue(user);
						} else {
							firestoreUserLiveData.postValue(authUser);
						}
					});
			return firestoreUserLiveData;
		});
	}

	@Override
	public LiveData<User> getFullUserProfile() {
		return fullUserProfile;
	}

	@Override
	public void getOrCreateUserProfile(
			@NonNull String uid,
			@Nullable String displayName,
			@Nullable String email,
			@Nullable String photoUrl,
			@NonNull ProfileOperationCallbacks callbacks
	) {
		firestoreUserDataSource.getOrCreateUserProfile(uid, displayName, email, photoUrl, new FirestoreUserDataSource.UserCallbacks() {
			@Override
			public void onSuccess(@NonNull User user) {
				callbacks.onSuccess(user);
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}

	@Override
	public void startObservingUserProfile() {
		firebaseAuthDataSource.startObserving();
	}

	@Override
	public void stopObservingUserProfile() {
		firebaseAuthDataSource.stopObserving();
		if (userProfileListenerRegistration != null) {
			userProfileListenerRegistration.remove();
			userProfileListenerRegistration = null;
		}
	}

	@Override
	public void updateUserDefaultLocation(
			double latitude,
			double longitude,
			@Nullable String addressName,
			@NonNull OperationCallbacks callbacks
	) {
		User currentUser = fullUserProfile.getValue();
		if (currentUser == null || currentUser.getId() == null) {
			callbacks.onError("User not logged in.");
			return;
		}

		firestoreUserDataSource.updateUserLocation(currentUser.getId(), latitude, longitude, addressName, new FirestoreUserDataSource.OperationCallbacks() {
			@Override
			public void onSuccess() {
				callbacks.onSuccess();
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}

	@Override
	public void toggleFavoriteStatus(@NonNull String postId, @NonNull OperationCallbacks callbacks) {
		User currentUser = fullUserProfile.getValue();
		if (currentUser == null || currentUser.getId() == null) {
			callbacks.onError("User not logged in.");
			return;
		}

		List<String> favorites = currentUser.getFavoritePosts();
		boolean shouldAdd = favorites == null || !favorites.contains(postId);

		firestoreUserDataSource.updateUserFavorites(currentUser.getId(), postId, shouldAdd, new FirestoreUserDataSource.OperationCallbacks() {
			@Override
			public void onSuccess() {
				callbacks.onSuccess();
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}

	@Override
	public void getUserById(String userId, @NonNull UserRepository.GetUserByIdCallbacks callbacks) {
		firestoreUserDataSource.getUserById(userId, new FirestoreUserDataSource.GetUserByIdCallbacks() {
			@Override
			public void onSuccess(@Nullable User user) {
				callbacks.onSuccess(user);
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}
}