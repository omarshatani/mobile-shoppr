package com.shoppr.domain.datasource;

import androidx.annotation.NonNull;

import com.shoppr.model.User;

public interface FirestoreUserDataSource {
    interface FirestoreOperationCallbacks {
        void onSuccess(@NonNull User user);

        void onError(@NonNull String message);

        void onNotFound();
    }

    void getUser(@NonNull String uid, @NonNull FirestoreOperationCallbacks callbacks);

    void createUser(@NonNull User user, @NonNull FirestoreOperationCallbacks callbacks); // Can be used for initial create

    void updateUser(@NonNull User user, @NonNull FirestoreOperationCallbacks callbacks); // For updates, including location
}