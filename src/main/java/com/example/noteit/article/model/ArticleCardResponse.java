package com.example.noteit.article.model;

import java.time.OffsetDateTime;

public record ArticleCardResponse(
        String id,
        String title,
        String contentPreview,
        String coverUrl,
        AuthorView author,
        SummaryView summary,
        long likeCount,
        long favoriteCount,
        boolean liked,
        boolean favorited,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
