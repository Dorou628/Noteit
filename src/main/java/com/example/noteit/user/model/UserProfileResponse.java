package com.example.noteit.user.model;

public record UserProfileResponse(
        String id,
        String nickname,
        String avatarUrl,
        String bio,
        long followerCount,
        long followingCount,
        long articleCount,
        boolean followed
) {
}
