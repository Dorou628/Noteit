package com.example.noteit.common.event;

import java.time.LocalDateTime;

public record EventOutboxDO(
        long id,
        String eventId,
        String eventType,
        String aggregateType,
        String aggregateId,
        String payload,
        int status,
        int retryCount,
        LocalDateTime nextRetryAt,
        String lastError,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
