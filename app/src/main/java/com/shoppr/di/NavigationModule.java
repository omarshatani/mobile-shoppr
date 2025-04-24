package com.shoppr.di;

import com.shoppr.navigation.AppNavigator;
import com.shoppr.navigation.Navigator;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class NavigationModule {
    @Binds
    @Singleton
    public abstract Navigator bindNavigator(AppNavigator impl);
}
