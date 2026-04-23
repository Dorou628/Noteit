package com.example.noteit.article.domain;

public record StoredArticleContent(
        String contentText,
        String contentStorageType,
        String contentObjectKey,
        String contentUrl,
        String contentFormat,
        String contentPreview
) {
}
