package com.example.noteit.common.auth;

import java.util.Optional;

public interface TokenService {

    TokenPair issueTokenPair(Long userId);

    TokenPair refreshTokenPair(String refreshToken);

    Optional<Long> parseAccessToken(String accessToken);

    void revokeRefreshToken(String refreshToken);
}
