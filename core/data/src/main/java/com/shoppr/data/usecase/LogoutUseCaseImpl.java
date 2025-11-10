package com.shoppr.data.usecase;

import com.shoppr.domain.repository.AuthenticationRepository;
import com.shoppr.domain.usecase.LogoutUseCase;

import javax.inject.Inject;

public class LogoutUseCaseImpl implements LogoutUseCase {

	private final AuthenticationRepository authenticationRepository;

	@Inject
	public LogoutUseCaseImpl(AuthenticationRepository authenticationRepository) {
		this.authenticationRepository = authenticationRepository;
	}

	@Override
	public void invoke() {
		authenticationRepository.logout();
	}
}