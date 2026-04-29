package com.example.noteit.article.event;

import com.example.noteit.common.event.DomainEvent;

import java.time.OffsetDateTime;

/**
 * 作用：表示文章更新领域事件，payload 会携带文章 ID 和作者 ID，便于后续详情缓存失效等下游扩展。
 */
public record ArticleUpdatedEvent(
        String aggregateId,
        Long authorId,
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
