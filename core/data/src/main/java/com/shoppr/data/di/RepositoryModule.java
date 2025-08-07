package com.shoppr.data.di;

import com.google.firebase.functions.FirebaseFunctions;
import com.shoppr.data.datasource.FirebaseAuthDataSourceImpl;
import com.shoppr.data.datasource.FirebaseStorageDataSourceImpl;
import com.shoppr.data.datasource.FirestorePostDataSourceImpl;
import com.shoppr.data.datasource.FirestoreUserDataSourceImpl;
import com.shoppr.data.repository.AuthenticationRepositoryImpl;
import com.shoppr.data.repository.LLMRepositoryImpl;
import com.shoppr.data.repository.PostRepositoryImpl;
import com.shoppr.data.repository.UserRepositoryImpl;
import com.shoppr.domain.repository.AuthenticationRepository;
import com.shoppr.domain.repository.LLMRepository;
import com.shoppr.domain.repository.PostRepository;
import com.shoppr.domain.repository.UserRepository;

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
	public UserRepository provideUserRepository(FirestoreUserDataSourceImpl firestoreUserDataSourceImpl, FirebaseAuthDataSourceImpl firebaseAuthDataSourceImpl) {
		return new UserRepositoryImpl(firestoreUserDataSourceImpl, firebaseAuthDataSourceImpl);
	}

	@Provides
	@Singleton
	public PostRepository providePostRepository(FirestorePostDataSourceImpl firestoreUserDataSourceImpl, FirebaseStorageDataSourceImpl firebaseStorageDataSourceImpl) {
		return new PostRepositoryImpl(firestoreUserDataSourceImpl, firebaseStorageDataSourceImpl);
	}

	@Provides
	@Singleton
	public LLMRepository provideLLMRepository() {
		return new LLMRepositoryImpl(FirebaseFunctions.getInstance());
	}

}
