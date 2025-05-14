package com.shoppr.domain;

import com.shoppr.data.repository.AuthenticationRepository;

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