package com.shoppr.data.datasource;

import com.shoppr.model.User;

public interface FirestoreUserDataSource {
    interface FirestoreOperationCallbacks {
        void onSuccess(User user);
        void onError(String message);
        void onNotFound(); // For get operations if user doesn't exist
    }
    void getUser(String uid, FirestoreOperationCallbacks callbacks);
    void createUser(User user, FirestoreOperationCallbacks callbacks); // Takes domain User
 }