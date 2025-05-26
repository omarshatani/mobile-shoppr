package com.shoppr.data.di;

import com.shoppr.data.usecase.CheckInitialNavigationUseCaseImpl;
import com.shoppr.data.usecase.CreateUserProfileUseCaseImpl;
import com.shoppr.data.usecase.GetCurrentDeviceLocationUseCaseImpl;
import com.shoppr.data.usecase.GetCurrentUserUseCaseImpl;
import com.shoppr.data.usecase.GetLLMSuggestionsUseCaseImpl;
import com.shoppr.data.usecase.HandleSignInResultUseCaseImpl;
import com.shoppr.data.usecase.LogoutUseCaseImpl;
import com.shoppr.data.usecase.ObserveAuthStateUseCaseImpl;
import com.shoppr.data.usecase.SavePostUseCaseImpl;
import com.shoppr.data.usecase.UpdateUserDefaultLocationUseCaseImpl;
import com.shoppr.domain.usecase.CheckInitialNavigationUseCase;
import com.shoppr.domain.usecase.CreateUserProfileUseCase;
import com.shoppr.domain.usecase.GetCurrentDeviceLocationUseCase;
import com.shoppr.domain.usecase.GetCurrentUserUseCase;
import com.shoppr.domain.usecase.GetLLMSuggestionsUseCase;
import com.shoppr.domain.usecase.HandleSignInResultUseCase;
import com.shoppr.domain.usecase.LogoutUseCase;
import com.shoppr.domain.usecase.ObserveAuthStateUseCase;
import com.shoppr.domain.usecase.SavePostUseCase;
import com.shoppr.domain.usecase.UpdateUserDefaultLocationUseCase;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class UseCaseModule {
    @Binds
    @Singleton
    public abstract ObserveAuthStateUseCase bindObserveAuthStateUseCase(ObserveAuthStateUseCaseImpl impl);

    @Binds
    @Singleton
    public abstract HandleSignInResultUseCase bindHandleSignInResultUseCase(HandleSignInResultUseCaseImpl impl);

    @Binds
    @Singleton
    public abstract CreateUserProfileUseCase bindCreateUserProfileUseCase(CreateUserProfileUseCaseImpl impl);

    @Binds
    @Singleton
    public abstract CheckInitialNavigationUseCase bindCheckInitialNavigationUseCase(CheckInitialNavigationUseCaseImpl impl);

    @Binds
    @Singleton
    public abstract GetCurrentUserUseCase bindGetCurrentUserUseCase(GetCurrentUserUseCaseImpl impl);

    @Binds
    @Singleton
    public abstract SavePostUseCase bindSavePostUseCase(SavePostUseCaseImpl impl);

    @Binds
    @Singleton
    public abstract GetLLMSuggestionsUseCase bindAnalyzePostTextUseCase(GetLLMSuggestionsUseCaseImpl impl);

    @Binds
    @Singleton
    public abstract GetCurrentDeviceLocationUseCase bindGetCurrentDeviceLocationUseCase(GetCurrentDeviceLocationUseCaseImpl impl);

    @Binds
    @Singleton
    public abstract UpdateUserDefaultLocationUseCase bindUpdateUserDefaultLocationUseCase(UpdateUserDefaultLocationUseCaseImpl impl);

    @Binds
    @Singleton
    public abstract LogoutUseCase bindLogoutUseCase(LogoutUseCaseImpl impl);
}
