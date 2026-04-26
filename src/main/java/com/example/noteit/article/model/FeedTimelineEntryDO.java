package com.example.noteit.article.model;

import java.time.LocalDateTime;

public record FeedTimelineEntryDO(
        long id,
        Long userId,
        long authorId,
        long articleId,
        LocalDateTime publishedAt,
        LocalDateTime createdAt
) {
}
