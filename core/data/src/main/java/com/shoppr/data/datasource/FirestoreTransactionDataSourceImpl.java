package com.shoppr.data.datasource;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shoppr.domain.datasource.FirestoreTransactionDataSource;
import com.shoppr.model.Transaction;

import javax.inject.Inject;

public class FirestoreTransactionDataSourceImpl implements FirestoreTransactionDataSource {
	private final static String TAG = "FirestoreTransactionDataSource";
	private final FirebaseFirestore db;
	private static final String TRANSACTION_COLLECTION = "transactions";


	@Inject
	public FirestoreTransactionDataSourceImpl(FirebaseFirestore db) {
		this.db = db;
	}

	public void getTransactions() {
		db.collection(TRANSACTION_COLLECTION).get().addOnSuccessListener(queryDocumentSnapshots -> {
					Log.d(TAG, String.valueOf(queryDocumentSnapshots.getDocuments().size()));
				}
		).addOnFailureListener(e -> {
			Log.e(TAG, "Error getting documents: ", e);
		});
	}

	@Override
	public void createTransaction(@NonNull Transaction transaction, @NonNull CreateTransactionCallbacks callbacks) {
		DocumentReference newTransactionRef = db.collection("transactions").document();

		transaction.setId(newTransactionRef.getId());

		newTransactionRef.set(transaction)
				.addOnSuccessListener(aVoid -> {
					callbacks.onSuccess(transaction);
				})
				.addOnFailureListener(e -> {
					callbacks.onError("Failed to create transaction: " + e.getMessage());
				});
	}
}