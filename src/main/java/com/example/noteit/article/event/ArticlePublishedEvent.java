package com.example.noteit.article.event;

import com.example.noteit.common.event.DomainEvent;

import java.time.OffsetDateTime;

/**
 * 作用：表示文章发布领域事件，payload 会携带文章 ID 和作者 ID，便于下游直接做 feed 分发。
 */
public record ArticlePublishedEvent(
        String aggregateId,
        Long authorId,
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
