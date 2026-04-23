package com.example.noteit.user.model;

import java.time.LocalDateTime;

public record UserProfileDO(
        long id,
        String nickname,
        String avatarUrl,
        String bio,
        long followerCount,
        long followingCount,
        long articleCount,
        int status,
        long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
