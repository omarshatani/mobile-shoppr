package com.shoppr.data.datasource;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.shoppr.domain.datasource.FirestoreRequestDataSource;
import com.shoppr.model.Request;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FirestoreRequestDataSourceImpl implements FirestoreRequestDataSource {

	private final FirebaseFirestore db;

	@Inject
	public FirestoreRequestDataSourceImpl(FirebaseFirestore db) {
		this.db = db;
	}

	@Override
	public void createRequest(@NonNull Request request, @NonNull RequestOperationCallbacks callbacks) {
		WriteBatch batch = db.batch();
		DocumentReference requestRef;

		if (request.getId() != null && !request.getId().isEmpty()) {
			requestRef = db.collection("requests").document(request.getId());
		} else {
			requestRef = db.collection("requests").document();
			request.setId(requestRef.getId());
		}

		batch.set(requestRef, request);

		DocumentReference postRef = db.collection("posts").document(request.getPostId());
		batch.update(postRef, "requests", FieldValue.arrayUnion(requestRef.getId()));
		batch.update(postRef, "offeringUserIds", FieldValue.arrayUnion(request.getBuyerId()));

		batch.commit()
				.addOnSuccessListener(aVoid -> callbacks.onSuccess(request))
				.addOnFailureListener(e -> callbacks.onError("Failed to submit offer: " + e.getMessage()));
	}

	@Override
	public LiveData<List<Request>> getRequestsForPost(@NonNull String postId) {
		MutableLiveData<List<Request>> requestsLiveData = new MutableLiveData<>();
		db.collection("requests")
				.whereEqualTo("postId", postId)
				.addSnapshotListener((value, error) -> {
					if (error != null) {
						Log.w("FirestoreRequestDataSource", "Listen failed.", error);
						requestsLiveData.setValue(new ArrayList<>());
						return;
					}
					if (value != null) {
						List<Request> requests = value.toObjects(Request.class);
						requestsLiveData.setValue(requests);
					}
				});
		return requestsLiveData;
	}

	@Override
	public void getRequestForPost(String userId, String postId, @NonNull SingleRequestCallback callbacks) {
		db.collection("requests")
				.whereEqualTo("buyerId", userId)
				.whereEqualTo("postId", postId)
				.limit(1)
				.get()
				.addOnSuccessListener(queryDocumentSnapshots -> {
					if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
						callbacks.onSuccess(queryDocumentSnapshots.getDocuments().get(0).toObject(Request.class));
					} else {
						callbacks.onSuccess(null);
					}
				})
				.addOnFailureListener(e -> callbacks.onError("Error fetching request: " + e.getMessage()));
	}
}