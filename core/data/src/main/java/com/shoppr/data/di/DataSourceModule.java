package com.shoppr.data.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.shoppr.data.adapter.FirebaseUserToUserMapper;
import com.shoppr.data.datasource.FirebaseAuthDataSourceImpl;
import com.shoppr.data.datasource.FirebaseStorageDataSourceImpl;
import com.shoppr.data.datasource.FirestorePostDataSourceImpl;
import com.shoppr.data.datasource.FirestoreUserDataSourceImpl;
import com.shoppr.domain.datasource.FirebaseAuthDataSource;
import com.shoppr.domain.datasource.FirestorePostDataSource;
import com.shoppr.domain.datasource.FirestoreUserDataSource;
import com.shoppr.domain.datasource.FirebaseStorageDataSource;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DataSourceModule {
	final FirebaseAuth auth = FirebaseAuth.getInstance();
	final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
	final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

	@Provides
	@Singleton
	public FirebaseAuthDataSource provideFirebaseAuthDataSource() {
		return new FirebaseAuthDataSourceImpl(auth, new FirebaseUserToUserMapper());
	}

	@Provides
	@Singleton
	public FirestoreUserDataSource provideFirebaseUserDataSource() {
		return new FirestoreUserDataSourceImpl(firestore);
	}

	@Provides
	@Singleton
	public FirestorePostDataSource provideFirestorePostDataSource() {
		return new FirestorePostDataSourceImpl(firestore);
	}

	@Provides
	@Singleton
	public FirebaseStorageDataSource provideStorageDataSource() {
		return new FirebaseStorageDataSourceImpl(firebaseStorage);
	}
}
