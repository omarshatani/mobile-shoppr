package com.shoppr.data.model;

public interface IAuthenticationRepository {
    boolean isUserLoggedIn();
    void logout();
}