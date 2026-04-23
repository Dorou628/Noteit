package com.example.noteit.article.model;

import java.time.LocalDateTime;

public record ArticleDetailDO(
        long id,
        long authorId,
        String title,
        String contentText,
        String contentStorageType,
        String contentObjectKey,
        String contentUrl,
        String contentFormat,
        String contentPreview,
        String coverUrl,
        long likeCount,
        long favoriteCount,
        int summaryStatus,
        String summaryText,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String authorNickname,
        String authorAvatarUrl
) {
}
