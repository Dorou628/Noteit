package com.example.noteit.summary.event;

import com.example.noteit.common.event.DomainEvent;

import java.time.OffsetDateTime;

public record ArticleSummaryRequestedEvent(
        String aggregateId,
        OffsetDateTime occurredAt
) implements DomainEvent {

    @Override
    public String eventType() {
        return "ArticleSummaryRequested";
    }

    @Override
    public String aggregateType() {
        return "ARTICLE";
    }
}
