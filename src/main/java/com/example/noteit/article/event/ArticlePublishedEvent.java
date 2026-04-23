package com.example.noteit.article.event;

import com.example.noteit.common.event.DomainEvent;

import java.time.OffsetDateTime;

public record ArticlePublishedEvent(
        String aggregateId,
        OffsetDateTime occurredAt
) implements DomainEvent {

    @Override
    public String eventType() {
        return "ArticlePublished";
    }

    @Override
    public String aggregateType() {
        return "ARTICLE";
    }
}
