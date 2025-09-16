package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;

import com.shoppr.model.Transaction;

public interface CreateTransactionUseCase {

	interface CreateTransactionCallbacks {
		void onSuccess(@NonNull Transaction transaction);

		void onError(@NonNull String message);
	}

	void execute(@NonNull Transaction transaction, @NonNull CreateTransactionCallbacks callbacks);
}