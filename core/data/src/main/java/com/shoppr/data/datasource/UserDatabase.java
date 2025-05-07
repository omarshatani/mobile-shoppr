package com.shoppr.data.datasource;

import com.google.firebase.firestore.FirebaseFirestore;
import com.shoppr.model.User;

public class UserDatabase {
	private final FirebaseFirestore db;

	public UserDatabase(FirebaseFirestore db) {
		this.db = db;
	}

	public void saveUser(User user) {
		db.collection("users").document(user.getId()).set(user);
	}

	public User getUser(String id) {
		return db.collection("users").document(id).get().getResult().toObject(User.class);
	}

	public void updateUser(User user) {
		db.collection("users").document(user.getId()).set(user);
	}

	public void deleteUser(String id) {
		db.collection("users").document(id).delete();
	}


}
