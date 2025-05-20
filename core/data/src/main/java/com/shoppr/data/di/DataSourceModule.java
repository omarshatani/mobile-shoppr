package com.shoppr.data.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shoppr.data.adapter.FirebaseUserToUserMapper;
import com.shoppr.data.datasource.FirebaseAuthDataSource;
import com.shoppr.data.datasource.FirebaseAuthDataSourceImpl;
import com.shoppr.data.datasource.FirestorePostDataSource;
import com.shoppr.data.datasource.FirestorePostDataSourceImpl;
import com.shoppr.data.datasource.FirestoreUserDataSource;
import com.shoppr.data.datasource.FirestoreUserDataSourceImpl;

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
}
