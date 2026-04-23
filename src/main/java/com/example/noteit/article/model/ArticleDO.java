package com.example.noteit.article.model;

import java.time.LocalDateTime;

public record ArticleDO(
        long id,
        long authorId,
        String title,
        String contentText,
        String contentStorageType,
        String contentObjectKey,
        String contentUrl,
        String contentFormat,
        String contentPreview,
        String coverObjectKey,
        String coverUrl,
        int status,
        long likeCount,
        long favoriteCount,
        int summaryStatus,
        String summaryText,
        long summaryVersion,
        int summaryRetryCount,
        String summaryLastError,
        LocalDateTime publishedAt,
        long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
