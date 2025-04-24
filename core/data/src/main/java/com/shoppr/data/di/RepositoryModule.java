package com.shoppr.data.di;

import com.google.firebase.auth.FirebaseAuth;
import com.shoppr.data.model.IAuthenticationRepository;
import com.shoppr.data.repository.AuthenticationRepository;

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
    public IAuthenticationRepository provideAuthenticationRepository(FirebaseAuth firebaseAuth) {
        return new AuthenticationRepository(firebaseAuth);
    }

}
