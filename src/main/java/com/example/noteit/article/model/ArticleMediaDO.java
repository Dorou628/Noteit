package com.example.noteit.article.model;

import java.time.LocalDateTime;

public record ArticleMediaDO(
        long id,
        long articleId,
        String mediaType,
        String objectKey,
        String mediaUrl,
        int sortNo,
        boolean isCover,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
