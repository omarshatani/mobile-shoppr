package com.shoppr.domain;

import com.shoppr.data.repository.AuthenticationRepository;

import javax.inject.Inject;

public class LogoutUseCaseImpl implements LogoutUseCase {
	private final AuthenticationRepository authenticationRepository;

	@Inject
	public LogoutUseCaseImpl(AuthenticationRepository authenticationRepository) {
		this.authenticationRepository = authenticationRepository;
	}

	public void invoke() {
		authenticationRepository.logout();
	}

}
