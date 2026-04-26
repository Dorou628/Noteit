package com.example.noteit.relation.event;

import com.example.noteit.common.event.DomainEvent;

import java.time.OffsetDateTime;

public record FollowRelationshipChangedEvent(
        long followerUserId,
        long followeeUserId,
        boolean following,
        OffsetDateTime occurredAt
) implements DomainEvent {

    @Override
    public String eventType() {
        return "FollowRelationshipChanged";
    }

    @Override
    public String aggregateType() {
        return "USER_FOLLOW";
    }

    @Override
    public String aggregateId() {
        return String.valueOf(followeeUserId);
    }
}
