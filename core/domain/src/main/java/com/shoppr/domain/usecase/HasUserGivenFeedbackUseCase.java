package com.shoppr.domain.usecase;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public interface HasUserGivenFeedbackUseCase {
	LiveData<Boolean> execute(@NonNull String rateeId, @NonNull String raterId);
}