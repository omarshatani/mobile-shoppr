package com.shoppr.data.datasource;

import com.shoppr.model.User;

public interface FirestoreUserDataSource { // Renamed from IUserDataSource
    interface FirestoreOperationCallbacks {
        void onSuccess(User user);

        void onError(String message);

        void onNotFound();
    }

    void getUser(String uid, FirestoreOperationCallbacks callbacks);

    void createUser(User user, FirestoreOperationCallbacks callbacks);
}