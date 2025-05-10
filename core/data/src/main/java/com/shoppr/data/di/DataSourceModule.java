package com.shoppr.data.di;

import com.google.firebase.auth.FirebaseAuth;
import com.shoppr.data.datasource.FirebaseAuthDataSource;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DataSourceModule {
	final FirebaseAuth auth = FirebaseAuth.getInstance();

	@Provides
	@Singleton
	public FirebaseAuthDataSource provideFirebaseAuthDataSource() {
		return new FirebaseAuthDataSource(auth);
	}
}
