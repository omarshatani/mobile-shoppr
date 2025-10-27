package com.shoppr.data.datasource;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.shoppr.domain.datasource.FirestoreFeedbackDataSource;
import com.shoppr.model.Feedback;
import com.shoppr.model.User;

import javax.inject.Inject;

public class FirestoreFeedbackDataSourceImpl implements FirestoreFeedbackDataSource {

	private final FirebaseFirestore db;
	private static final String FEEDBACK_COLLECTION = "feedback";
	private static final String USERS_COLLECTION = "users"; // Added for submitFeedback transaction

	@Inject
	public FirestoreFeedbackDataSourceImpl(FirebaseFirestore db) {
		this.db = db;
	}

	// Submit feedback using Firestore Transaction
	@Override
	public void submitFeedback(@NonNull Feedback feedback, @NonNull SubmitFeedbackCallbacks callbacks) {
		db.runTransaction((Transaction.Function<Void>) transaction -> {
					// 1. Get a reference to the user who is being rated
					DocumentReference userRef = db.collection(USERS_COLLECTION).document(feedback.getRateeId());
					DocumentSnapshot userSnapshot = transaction.get(userRef);
					User user = userSnapshot.toObject(User.class);

					if (user == null) {
						// Use a more specific exception or handle null user appropriately
						throw new com.google.firebase.firestore.FirebaseFirestoreException("Rated user not found!",
								com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND);
					}

					// 2. Calculate the new average rating
					int oldRatingCount = user.getRatingCount();
					double oldAverageRating = user.getAverageRating();
					double newAverageRating = ((oldAverageRating * oldRatingCount) + feedback.getRating()) / (oldRatingCount + 1);
					int newRatingCount = oldRatingCount + 1;

					// 3. Create the new Feedback document reference and set ID
					DocumentReference feedbackRef = db.collection(FEEDBACK_COLLECTION).document();
					feedback.setId(feedbackRef.getId());

					// 4. Perform writes within the transaction
					transaction.set(feedbackRef, feedback);
					transaction.update(userRef, "averageRating", newAverageRating);
					transaction.update(userRef, "ratingCount", newRatingCount);

					return null; // Transaction success
				})
				.addOnSuccessListener(aVoid -> callbacks.onSuccess())
				.addOnFailureListener(e -> callbacks.onError("Failed to submit feedback: " + e.getMessage()));
	}


	// Provides LiveData that listens for feedback status
	@Override
	public LiveData<Boolean> hasUserGivenFeedback(@NonNull String requestId, @NonNull String raterId) {
		return new FeedbackStatusLiveData(db, requestId, raterId);
	}

	// Custom LiveData that registers and unregisters the Firestore listener
	private static class FeedbackStatusLiveData extends LiveData<Boolean> {
		private final FirebaseFirestore db;
		private final String requestId;
		private final String raterId;
		private ListenerRegistration listenerRegistration;

		FeedbackStatusLiveData(FirebaseFirestore db, String requestId, String raterId) {
			this.db = db;
			this.requestId = requestId;
			this.raterId = raterId;
			setValue(false); // Initial assumption
		}

		@Override
		protected void onActive() {
			super.onActive();
			// Register listener when LiveData becomes active
			Query query = db.collection(FEEDBACK_COLLECTION)
					.whereEqualTo("requestId", requestId) // Ensure this field name is correct in Firestore
					.whereEqualTo("raterId", raterId)
					.limit(1);

			listenerRegistration = query.addSnapshotListener((snapshots, error) -> {
				if (error != null) {
					Log.w("FeedbackStatusLiveData", "Listen failed.", error);
					setValue(false); // Assume false on error
					return;
				}
				// Update value based on whether any matching documents exist
				setValue(snapshots != null && !snapshots.isEmpty());
			});
		}

		@Override
		protected void onInactive() {
			super.onInactive();
			// Unregister listener when LiveData becomes inactive
			if (listenerRegistration != null) {
				listenerRegistration.remove();
				listenerRegistration = null;
			}
		}
	}
}