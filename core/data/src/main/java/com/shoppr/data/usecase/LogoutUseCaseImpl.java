package com.shoppr.data.usecase;

import com.shoppr.domain.AuthenticationRepository;
import com.shoppr.domain.LogoutUseCase;

import javax.inject.Inject;

public class LogoutUseCaseImpl implements LogoutUseCase {
	private final AuthenticationRepository authenticationRepository; // Using renamed interface

	@Inject
	public LogoutUseCaseImpl(AuthenticationRepository authenticationRepository) {
		this.authenticationRepository = authenticationRepository;
	}

	@Override
	public void invoke() {
		authenticationRepository.logout();
	}
}