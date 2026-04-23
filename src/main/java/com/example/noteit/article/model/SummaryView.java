package com.example.noteit.article.model;

import com.example.noteit.common.constant.SummaryStatus;

import java.time.OffsetDateTime;

public record SummaryView(
        SummaryStatus status,
        String content,
        OffsetDateTime updatedAt
) {
}
