package com.example.noteit.article.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ArticleImageRequest(
        @NotBlank String objectKey,
        @NotBlank String url,
        @NotNull Integer sortNo,
        // Keep backward compatibility with both "cover" and "isCover" payloads.
        @JsonProperty("cover") @JsonAlias("isCover") boolean isCover
) {
}
