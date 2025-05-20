package com.shoppr.domain.di;

import com.shoppr.domain.CheckInitialNavigationUseCase;
import com.shoppr.domain.CheckInitialNavigationUseCaseImpl;
import com.shoppr.domain.CreateUserProfileUseCase;
import com.shoppr.domain.CreateUserProfileUseCaseImpl;
import com.shoppr.domain.GetCurrentUserUseCase;
import com.shoppr.domain.GetCurrentUserUseCaseImpl;
import com.shoppr.domain.HandleSignInResultUseCase;
import com.shoppr.domain.HandleSignInResultUseCaseImpl;
import com.shoppr.domain.LogoutUseCase;
import com.shoppr.domain.LogoutUseCaseImpl;
import com.shoppr.domain.ObserveAuthStateUseCase;
import com.shoppr.domain.ObserveAuthStateUseCaseImpl;
import com.shoppr.domain.SavePostUseCase;
import com.shoppr.domain.SavePostUseCaseImpl;

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
	public abstract LogoutUseCase bindLogoutUseCase(LogoutUseCaseImpl impl);
}
