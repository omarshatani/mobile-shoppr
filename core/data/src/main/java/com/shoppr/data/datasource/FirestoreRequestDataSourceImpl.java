package com.shoppr.data.datasource;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.shoppr.domain.datasource.FirestoreRequestDataSource;
import com.shoppr.model.Request;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirestoreRequestDataSourceImpl implements FirestoreRequestDataSource {

	private final FirebaseFirestore firestore;
	private static final String REQUESTS_COLLECTION = "requests";

	@Inject
	public FirestoreRequestDataSourceImpl(FirebaseFirestore firestore) {
		this.firestore = firestore;
	}

	@Override
	public void createRequest(@NonNull Request request, @NonNull RequestOperationCallbacks callbacks) {
		firestore.collection(REQUESTS_COLLECTION)
				.add(request)
				.addOnSuccessListener(documentReference -> {
					String newId = documentReference.getId();
					request.setId(newId);
					// Update the document to include its own ID as a field
					documentReference.update("id", newId)
							.addOnSuccessListener(aVoid -> callbacks.onSuccess(request))
							.addOnFailureListener(e -> callbacks.onError("Failed to update request with ID: " + e.getMessage()));
				})
				.addOnFailureListener(e -> callbacks.onError("Failed to create request: " + e.getMessage()));
	}

	@Override
	public LiveData<List<Request>> getRequestsForPost(@NonNull String postId) {
		MutableLiveData<List<Request>> requestsLiveData = new MutableLiveData<>();
		firestore.collection(REQUESTS_COLLECTION)
				.whereEqualTo("postId", postId)
				.orderBy("createdAt", Query.Direction.DESCENDING)
				.addSnapshotListener((snapshots, e) -> {
					if (e != null) {
						requestsLiveData.postValue(null);
						return;
					}
					List<Request> requests = new ArrayList<>();
					if (snapshots != null) {
						for (QueryDocumentSnapshot document : snapshots) {
							requests.add(document.toObject(Request.class));
						}
					}
					requestsLiveData.postValue(requests);
				});
		return requestsLiveData;
	}
}