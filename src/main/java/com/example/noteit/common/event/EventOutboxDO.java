package com.example.noteit.common.event;

import java.time.LocalDateTime;

/**
 * 作用：表示 event_outbox 表中的一行事件记录。
 * 输入：字段与数据库列一一对应。
 * 输出：供 worker、Canal 消费器、Kafka 分发器和业务 handler 传递事件上下文。
 */
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
        String lockedBy,
        LocalDateTime lockedUntil,
        String lastError,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
