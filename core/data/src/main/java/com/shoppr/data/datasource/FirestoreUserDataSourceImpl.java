package com.shoppr.data.datasource;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.shoppr.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirestoreUserDataSourceImpl implements FirestoreUserDataSource {
	private static final String TAG = "FirestoreUserDS";
	private static final String USERS_COLLECTION = "users";
	private final FirebaseFirestore firestore;

	@Inject
	public FirestoreUserDataSourceImpl(FirebaseFirestore firestore) {
		this.firestore = firestore;
	}

	@Override
	public void getUser(String uid, FirestoreOperationCallbacks callbacks) {
		Log.d(TAG, "Getting user from Firestore: " + uid);
		firestore.collection(USERS_COLLECTION).document(uid).get()
				.addOnSuccessListener(documentSnapshot -> {
					if (documentSnapshot.exists()) {
						User user = documentSnapshot.toObject(User.class);
						if (user != null) {
							if (user.getId() == null) user.setId(uid); // Ensure ID is set
							callbacks.onSuccess(user);
						} else {
							callbacks.onError("Failed to map Firestore document to User object for UID: " + uid);
						}
					} else {
						callbacks.onNotFound();
					}
				})
				.addOnFailureListener(e -> callbacks.onError("Error fetching user " + uid + ": " + e.getMessage()));
	}

	@Override
	public void createUser(User user, FirestoreOperationCallbacks callbacks) {
		Log.d(TAG, "Creating user in Firestore: " + user.getId());
		firestore.collection(USERS_COLLECTION).document(user.getId()).set(user)
				.addOnSuccessListener(aVoid -> callbacks.onSuccess(user))
				.addOnFailureListener(e -> callbacks.onError("Error creating user " + user.getId() + ": " + e.getMessage()));
	}
}
