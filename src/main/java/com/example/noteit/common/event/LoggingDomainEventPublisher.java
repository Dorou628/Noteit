package com.example.noteit.common.event;

import com.example.noteit.article.event.ArticleDeletedEvent;
import com.example.noteit.article.event.ArticlePublishedEvent;
import com.example.noteit.article.event.ArticleUpdatedEvent;
import com.example.noteit.common.id.IdGenerator;
import com.example.noteit.common.util.TimeProvider;
import com.example.noteit.relation.event.FollowRelationshipChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

    private final EventOutboxRepository eventOutboxRepository;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public LoggingDomainEventPublisher(
            EventOutboxRepository eventOutboxRepository,
            IdGenerator idGenerator,
            TimeProvider timeProvider
    ) {
        this.eventOutboxRepository = eventOutboxRepository;
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    /**
     * 作用：把领域事件持久化到 event_outbox 表，后续由 worker 或 Canal 链路异步分发。
     * 输入：event 为业务代码发布的领域事件。
     * 输出：无返回值；写库失败时抛出异常并回滚当前事务。
     */
    public void publish(DomainEvent event) {
        long id = idGenerator.nextId();
        eventOutboxRepository.add(new EventOutboxDO(
                id,
                String.valueOf(id),
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                serialize(event),
                EventOutboxStatus.NEW,
                0,
                null,
                null,
                null,
                null,
                null,
                null
        ));
        log.info("Saved domain event to outbox: type={}, aggregateType={}, aggregateId={}",
                event.eventType(), event.aggregateType(), event.aggregateId());
    }

    /**
     * 作用：把领域事件转换为 outbox payload JSON。
     * 输入：event 为待持久化事件。
     * 输出：JSON 字符串；文章事件会额外包含 authorId，关注事件会包含关注双方 ID。
     */
    private String serialize(DomainEvent event) {
        if (event instanceof ArticlePublishedEvent articleEvent) {
            return serializeArticleEvent(event, articleEvent.authorId());
        }
        if (event instanceof ArticleDeletedEvent articleEvent) {
            return serializeArticleEvent(event, articleEvent.authorId());
        }
        if (event instanceof ArticleUpdatedEvent articleEvent) {
            return serializeArticleEvent(event, articleEvent.authorId());
        }
        if (event instanceof FollowRelationshipChangedEvent followEvent) {
            return """
                    {"eventType":"%s","aggregateType":"%s","aggregateId":"%s","followerUserId":%d,"followeeUserId":%d,"following":%s,"occurredAt":"%s"}
                    """.formatted(
                    event.eventType(),
                    event.aggregateType(),
                    event.aggregateId(),
                    followEvent.followerUserId(),
                    followEvent.followeeUserId(),
                    followEvent.following(),
                    followEvent.occurredAt()
            );
        }
        return """
                {"eventType":"%s","aggregateType":"%s","aggregateId":"%s","occurredAt":"%s"}
                """.formatted(
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                event.occurredAt() == null ? timeProvider.now() : event.occurredAt()
        );
    }

    /**
     * 作用：序列化文章类事件，确保 payload 中包含 authorId。
     * 输入：event 为文章领域事件，authorId 为文章作者 ID。
     * 输出：包含 eventType、aggregateType、aggregateId、authorId、occurredAt 的 JSON 字符串。
     */
    private String serializeArticleEvent(DomainEvent event, Long authorId) {
        return """
                {"eventType":"%s","aggregateType":"%s","aggregateId":"%s","authorId":%d,"occurredAt":"%s"}
                """.formatted(
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                authorId,
                event.occurredAt() == null ? timeProvider.now() : event.occurredAt()
        );
    }
}
