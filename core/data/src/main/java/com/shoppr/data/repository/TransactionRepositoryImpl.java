package com.shoppr.data.repository;

import androidx.annotation.NonNull;

import com.shoppr.domain.datasource.FirestoreTransactionDataSource;
import com.shoppr.domain.repository.TransactionRepository;
import com.shoppr.model.Transaction;

import javax.inject.Inject;

public class TransactionRepositoryImpl implements TransactionRepository {

	private final FirestoreTransactionDataSource firestoreTransactionDataSource;

	@Inject
	public TransactionRepositoryImpl(FirestoreTransactionDataSource firestoreTransactionDataSource) {
		this.firestoreTransactionDataSource = firestoreTransactionDataSource;
	}

	@Override
	public void getTransactions() {
		firestoreTransactionDataSource.getTransactions();
	}

	@Override
	public void createTransaction(@NonNull Transaction transaction, @NonNull CreateTransactionCallbacks callbacks) {
		firestoreTransactionDataSource.createTransaction(transaction, new FirestoreTransactionDataSource.CreateTransactionCallbacks() {
			@Override
			public void onSuccess(@NonNull Transaction transaction) {
				callbacks.onSuccess(transaction);
			}

			@Override
			public void onError(@NonNull String message) {
				callbacks.onError(message);
			}
		});
	}
}