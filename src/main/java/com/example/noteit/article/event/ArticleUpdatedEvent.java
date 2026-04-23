package com.example.noteit.article.event;

import com.example.noteit.common.event.DomainEvent;

import java.time.OffsetDateTime;

public record ArticleUpdatedEvent(
        String aggregateId,
        OffsetDateTime occurredAt
) implements DomainEvent {

    @Override
    public String eventType() {
        return "ArticleUpdated";
    }

    @Override
    public String aggregateType() {
        return "ARTICLE";
    }
}
