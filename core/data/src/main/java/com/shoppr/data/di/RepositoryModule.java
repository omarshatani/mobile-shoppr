package com.shoppr.data.di;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.shoppr.data.datasource.FirebaseAuthDataSourceImpl;
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
	public UserRepository provideUserRepository() {
		return new UserRepositoryImpl(new FirestoreUserDataSourceImpl(FirebaseFirestore.getInstance()));
	}

	@Provides
	@Singleton
	public PostRepository providePostRepository() {
		return new PostRepositoryImpl(new FirestorePostDataSourceImpl(FirebaseFirestore.getInstance()));
	}

	@Provides
	@Singleton
	public LLMRepository provideLLMRepository() {
		return new LLMRepositoryImpl(FirebaseFunctions.getInstance());
	}

}
