package com.example.noteit.common.auth;

public interface AuthFacade {

    TokenPair login(Long userId);

    TokenPair refresh(String refreshToken);

    void logout(String refreshToken);
}
