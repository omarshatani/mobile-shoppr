package com.shoppr.data.usecase;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.shoppr.domain.repository.AuthenticationRepository;
import com.shoppr.domain.usecase.ObserveAuthStateUseCase;
import com.shoppr.model.User;

import javax.inject.Inject;

 public class ObserveAuthStateUseCaseImpl implements ObserveAuthStateUseCase {
    private static final String TAG = "ObserveAuthUCImpl";
    private final AuthenticationRepository authRepository;

    // This LiveData directly comes from the AuthenticationRepository
    private final LiveData<User> rawAuthUserLiveData;

    @Inject
    public ObserveAuthStateUseCaseImpl(AuthenticationRepository authRepository) {
        this.authRepository = authRepository;
        this.rawAuthUserLiveData = authRepository.getRawAuthState();
        Log.d(TAG, "Initialized. Will expose rawAuthUserLiveData from AuthenticationRepository.");
    }

    @Override
    public LiveData<User> getRawAuthUser() {
        return rawAuthUserLiveData;
    }

    @Override
    public void startObserving() {
        Log.d(TAG, "startObserving called. Delegating to authRepository.startObservingAuthState()");
        authRepository.startObservingAuthState();
    }

    @Override
    public void stopObserving() {
        Log.d(TAG, "stopObserving called. Delegating to authRepository.stopObservingAuthState()");
        authRepository.stopObservingAuthState();
    }
 }