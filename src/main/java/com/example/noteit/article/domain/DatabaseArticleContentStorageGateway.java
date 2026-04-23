package com.example.noteit.article.domain;

import com.example.noteit.article.model.ArticleContentStorageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DatabaseArticleContentStorageGateway implements ArticleContentStorageGateway {

    private static final int DEFAULT_PREVIEW_LENGTH = 120;

    private final String contentStorageMode;

    public DatabaseArticleContentStorageGateway(
            @Value("${noteit.article.content-storage-mode:DB}") String contentStorageMode
    ) {
        this.contentStorageMode = contentStorageMode;
    }

    @Override
    public StoredArticleContent store(String content, String contentFormat, String contentPreview) {
        if (!ArticleContentStorageType.DB.name().equalsIgnoreCase(contentStorageMode)) {
            // TODO: Add an OSS-backed gateway and delegate by storage mode when body storage returns to OSS.
            throw new IllegalStateException("Unsupported article content storage mode: " + contentStorageMode);
        }

        return new StoredArticleContent(
                content,
                ArticleContentStorageType.DB.name(),
                null,
                null,
                contentFormat,
                buildPreview(content, contentPreview)
        );
    }

    @Override
    public String load(String contentStorageType, String contentText, String contentObjectKey, String contentUrl) {
        if (ArticleContentStorageType.DB.name().equalsIgnoreCase(contentStorageType)) {
            return contentText;
        }

        // TODO: Load article content from OSS when content storage mode switches back to object storage.
        return null;
    }

    private String buildPreview(String content, String preferredPreview) {
        if (StringUtils.hasText(preferredPreview)) {
            return preferredPreview.trim();
        }

        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= DEFAULT_PREVIEW_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, DEFAULT_PREVIEW_LENGTH);
    }
}
