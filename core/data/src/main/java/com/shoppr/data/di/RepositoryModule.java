package com.shoppr.data.di;

import com.google.firebase.firestore.FirebaseFirestore;
import com.shoppr.data.datasource.FirebaseAuthDataSourceImpl;
import com.shoppr.data.datasource.FirestoreUserDataSourceImpl;
import com.shoppr.data.repository.AuthenticationRepository;
import com.shoppr.data.repository.AuthenticationRepositoryImpl;
import com.shoppr.data.repository.UserRepository;
import com.shoppr.data.repository.UserRepositoryImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

	@Provides
	@Singleton
	public AuthenticationRepository provideAuthenticationRepository(FirebaseAuthDataSourceImpl firebaseAuthDataSourceImpl) {
		return new AuthenticationRepositoryImpl(firebaseAuthDataSourceImpl);
	}

	@Provides
	@Singleton
	public UserRepository provideUserRepository() {
		return new UserRepositoryImpl(new FirestoreUserDataSourceImpl(FirebaseFirestore.getInstance()));
	}

}
