package com.shoppr.data.datasource;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.shoppr.domain.datasource.FirestoreRequestDataSource;
import com.shoppr.model.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
	public void deleteRequest(@NonNull Request request, @NonNull RequestDeleteCallbacks callbacks) {
		WriteBatch batch = db.batch();

		DocumentReference requestRef = db.collection("requests").document(request.getId());
		batch.delete(requestRef);

		DocumentReference postRef = db.collection("posts").document(request.getPostId());
		batch.update(postRef, "requests", FieldValue.arrayRemove(request.getId()));
		batch.update(postRef, "offeringUserIds", FieldValue.arrayRemove(request.getBuyerId()));

		batch.commit()
				.addOnSuccessListener(aVoid -> callbacks.onSuccess())
				.addOnFailureListener(e -> callbacks.onError("Failed to withdraw offer: " + e.getMessage()));
	}

	@Override
	public void updateRequest(@NonNull Request request, @NonNull RequestUpdateCallbacks callbacks) {
		if (request.getId() == null) {
			callbacks.onError("Cannot update request with null ID.");
			return;
		}
		db.collection("requests").document(request.getId())
				.set(request)
				.addOnSuccessListener(aVoid -> callbacks.onSuccess())
				.addOnFailureListener(e -> callbacks.onError("Failed to update request: " + e.getMessage()));
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
	public LiveData<List<Request>> getAllRequestsForUser(@NonNull String userId) {
		MutableLiveData<List<Request>> requestsLiveData = new MutableLiveData<>();

		Query buyerQuery = db.collection("requests").whereEqualTo("buyerId", userId);
		Query sellerQuery = db.collection("requests").whereEqualTo("sellerId", userId);

		// Combine the results of both queries
		buyerQuery.addSnapshotListener((buyerSnapshots, e1) -> {
			if (e1 != null) {
				Log.w("FirestoreRequestDataSource", "Buyer query listen failed.", e1);
				return;
			}

			sellerQuery.addSnapshotListener((sellerSnapshots, e2) -> {
				if (e2 != null) {
					Log.w("FirestoreRequestDataSource", "Seller query listen failed.", e2);
					return;
				}

				List<Request> allRequests = new ArrayList<>();
				if (buyerSnapshots != null) {
					allRequests.addAll(buyerSnapshots.toObjects(Request.class));
				}
				if (sellerSnapshots != null) {
					allRequests.addAll(sellerSnapshots.toObjects(Request.class));
				}

				// Simple de-duplication in case a user makes an offer to themselves (edge case)
				List<Request> distinctRequests = allRequests.stream()
						.distinct().sorted((r1, r2) -> {
							if (r1.getCreatedAt() != null && r2.getCreatedAt() != null) {
								return r2.getCreatedAt().compareTo(r1.getCreatedAt());
							}
							return 0;
						}).collect(Collectors.toList());

				requestsLiveData.setValue(distinctRequests);
			});
		});

		return requestsLiveData;
	}

	@Override
	public LiveData<Request> getRequestById(@NonNull String requestId) {
		MutableLiveData<Request> requestLiveData = new MutableLiveData<>();
		db.collection("requests").document(requestId)
				.addSnapshotListener((snapshot, e) -> {
					if (e != null) {
						Log.w("FirestoreRequestDataSource", "Listen failed.", e);
						requestLiveData.setValue(null);
						return;
					}
					if (snapshot != null && snapshot.exists()) {
						requestLiveData.setValue(snapshot.toObject(Request.class));
					} else {
						requestLiveData.setValue(null);
					}
				});
		return requestLiveData;
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