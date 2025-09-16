package com.shoppr.data.usecase;

import androidx.annotation.NonNull;

import com.shoppr.domain.repository.TransactionRepository;
import com.shoppr.domain.usecase.CreateTransactionUseCase;
import com.shoppr.model.Transaction;

import javax.inject.Inject;

public class CreateTransactionUseCaseImpl implements CreateTransactionUseCase {

	private final TransactionRepository transactionRepository;

	@Inject
	public CreateTransactionUseCaseImpl(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	@Override
	public void execute(@NonNull Transaction transaction, @NonNull CreateTransactionCallbacks callbacks) {
		transactionRepository.createTransaction(transaction, new TransactionRepository.CreateTransactionCallbacks() {
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