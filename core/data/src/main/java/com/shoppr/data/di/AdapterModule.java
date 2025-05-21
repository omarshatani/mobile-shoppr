package com.shoppr.data.di;

import com.shoppr.data.adapter.FirebaseUserToUserMapper;
import com.shoppr.domain.FirebaseUserMapper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AdapterModule {
	@Provides
	@Singleton
	public FirebaseUserMapper provideFirebaseUserMapper() {
		return new FirebaseUserToUserMapper();
	}
}
