package com.example.noteit.article.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateArticleRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotBlank String contentFormat,
        String contentPreview,
        String coverObjectKey,
        String coverUrl,
        @Valid List<ArticleImageRequest> images
) {
}
