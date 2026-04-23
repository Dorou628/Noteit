package com.example.noteit.relation.model;

import java.time.LocalDateTime;

public record UserFollowDO(
        long id,
        long followerId,
        long followeeId,
        int status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
