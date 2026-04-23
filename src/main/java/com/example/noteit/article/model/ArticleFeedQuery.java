package com.example.noteit.article.model;

import jakarta.validation.constraints.Min;

public record ArticleFeedQuery(
        @Min(1) Integer pageNo,
        @Min(1) Integer pageSize,
        String authorId
) {

    public int normalizedPageNo() {
        return pageNo == null ? 1 : pageNo;
    }

    public int normalizedPageSize() {
        return pageSize == null ? 10 : Math.min(pageSize, 20);
    }
}
