package com.example.noteit.interaction.model;

import java.time.LocalDateTime;

public record ArticleFavoriteDO(
        long id,
        long articleId,
        long userId,
        int status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
