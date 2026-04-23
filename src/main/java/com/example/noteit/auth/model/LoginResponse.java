package com.example.noteit.auth.model;

public record LoginResponse(
        String userId,
        String nickname,
        String avatarUrl,
        String authMode,
        String accessToken,
        String refreshToken
) {
}
