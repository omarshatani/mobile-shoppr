package com.shoppr.di;

import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class LocationModule {
	@Provides
	@Singleton
	public FusedLocationProviderClient provideFusedLocationProviderClient(
			@ApplicationContext Context context
	) {
		return LocationServices.getFusedLocationProviderClient(context);
	}
}
