package com.shoppr.data.datasource;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shoppr.domain.datasource.FirestoreFeedbackDataSource;
import com.shoppr.model.Feedback;
import com.shoppr.model.User;

import javax.inject.Inject;

public class FirestoreFeedbackDataSourceImpl implements FirestoreFeedbackDataSource {

	private final FirebaseFirestore db;
	private static final String FEEDBACK_COLLECTION = "feedback";

	@Inject
	public FirestoreFeedbackDataSourceImpl(FirebaseFirestore db) {
		this.db = db;
	}

	@Override
	public void submitFeedback(@NonNull Feedback feedback, @NonNull SubmitFeedbackCallbacks callbacks) {
		// Use a Firestore Transaction to ensure data consistency
		db.runTransaction(transaction -> {
					// 1. Get a reference to the user who is being rated
					DocumentReference userRef = db.collection("users").document(feedback.getRateeId());
					DocumentSnapshot userSnapshot = transaction.get(userRef);
					User user = userSnapshot.toObject(User.class);

					if (user == null) {
						throw new IllegalStateException("Rated user not found!");
					}

					// 2. Calculate the new average rating
					int oldRatingCount = user.getRatingCount();
					double oldAverageRating = user.getAverageRating();
					double newAverageRating = ((oldAverageRating * oldRatingCount) + feedback.getRating()) / (oldRatingCount + 1);
					int newRatingCount = oldRatingCount + 1;

					// 3. Create the new Feedback document
					DocumentReference feedbackRef = db.collection("feedback").document();
					feedback.setId(feedbackRef.getId());
					transaction.set(feedbackRef, feedback);

					// 4. Update the user's aggregated rating fields
					transaction.update(userRef, "averageRating", newAverageRating);
					transaction.update(userRef, "ratingCount", newRatingCount);

					return null; // Transaction success
				})
				.addOnSuccessListener(aVoid -> callbacks.onSuccess())
				.addOnFailureListener(e -> callbacks.onError("Failed to submit feedback: " + e.getMessage()));
	}

	@Override
	public LiveData<Boolean> hasUserGivenFeedback(@NonNull String requestId, @NonNull String raterId) {
		MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();

		db.collection(FEEDBACK_COLLECTION)
				.whereEqualTo("requestId", requestId)
				.whereEqualTo("raterId", raterId)
				.limit(1)
				.get()
				.addOnSuccessListener(queryDocumentSnapshots -> {
					resultLiveData.setValue(!queryDocumentSnapshots.isEmpty());
				})
				.addOnFailureListener(e -> {
					resultLiveData.setValue(false);
				});

		return resultLiveData;
	}
}