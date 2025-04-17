package com.shoppr.data.di;

import com.shoppr.data.model.IRepositoryFactory;
import com.shoppr.data.repository.AuthenticationRepository;
import com.shoppr.data.repository.ListingRepository;
import com.shoppr.data.repository.UserRepository;

public class RepositoryFactory implements IRepositoryFactory {
	@Override
	public UserRepository createUserRepository() {
		return null;
	}

	@Override
	public ListingRepository createListingRepository() {
		return null;
	}

	@Override
	public AuthenticationRepository createAuthenticationRepository() {
		return null;
	}

}
