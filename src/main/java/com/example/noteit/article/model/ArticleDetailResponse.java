package com.example.noteit.article.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ArticleDetailResponse(
        String id,
        String title,
        String content,
        String contentObjectKey,
        String contentUrl,
        String contentFormat,
        String contentPreview,
        String coverUrl,
        List<String> imageUrls,
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
