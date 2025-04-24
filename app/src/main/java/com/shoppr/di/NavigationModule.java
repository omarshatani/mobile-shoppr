package com.shoppr.di;

import com.shoppr.navigation.AppNavigator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class NavigationModule {
    @Provides
    @Singleton
    public AppNavigator provideNavigator() {
        return new AppNavigator();
    }
}
