package com.shoppr.domain.repository;

import androidx.annotation.NonNull;

import com.shoppr.model.Transaction;

public interface TransactionRepository {
	interface CreateTransactionCallbacks {
		void onSuccess(@NonNull Transaction transaction);

		void onError(@NonNull String message);
	}

	void getTransactions();

	void createTransaction(@NonNull Transaction transaction, @NonNull CreateTransactionCallbacks callbacks);
}