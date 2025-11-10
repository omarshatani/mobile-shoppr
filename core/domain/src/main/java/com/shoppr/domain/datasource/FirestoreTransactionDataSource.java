package com.shoppr.domain.datasource;

import androidx.annotation.NonNull;

import com.shoppr.model.Transaction;

public interface FirestoreTransactionDataSource {
	interface CreateTransactionCallbacks {
		void onSuccess(@NonNull Transaction transaction);

		void onError(@NonNull String message);
	}

	void getTransactions();

	void createTransaction(@NonNull Transaction transaction, @NonNull CreateTransactionCallbacks callbacks);
}