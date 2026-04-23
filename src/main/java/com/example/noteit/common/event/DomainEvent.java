package com.example.noteit.common.event;

import java.time.OffsetDateTime;

public interface DomainEvent {

    String eventType();

    String aggregateType();

    String aggregateId();

    OffsetDateTime occurredAt();
}
