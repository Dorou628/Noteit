package com.example.noteit.common.auth;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
