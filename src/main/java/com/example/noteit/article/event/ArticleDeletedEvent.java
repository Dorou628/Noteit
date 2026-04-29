package com.example.noteit.article.event;

import com.example.noteit.common.event.DomainEvent;

import java.time.OffsetDateTime;

/**
 * 作用：表示文章删除领域事件，payload 会携带文章 ID 和作者 ID，便于下游直接移除 feed 时间线。
 */
public record ArticleDeletedEvent(
        String aggregateId,
        Long authorId,
        OffsetDateTime occurredAt
) implements DomainEvent {

    @Override
    public String eventType() {
        return "ArticleDeleted";
    }

    @Override
    public String aggregateType() {
        return "ARTICLE";
    }
}
