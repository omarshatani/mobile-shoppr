package com.shoppr.data.model;

import com.shoppr.data.repository.AuthenticationRepository;
import com.shoppr.data.repository.ListingRepository;
import com.shoppr.data.repository.UserRepository;

public interface IRepositoryFactory {
	UserRepository createUserRepository();
	ListingRepository createListingRepository();

	AuthenticationRepository createAuthenticationRepository();
}
