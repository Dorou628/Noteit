package com.example.noteit.common.event;

import com.example.noteit.article.event.ArticlePublishedEvent;
import com.example.noteit.common.id.IdGenerator;
import com.example.noteit.common.util.TimeProvider;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingDomainEventPublisherTest {

    /**
     * 作用：验证文章事件写入 outbox 时会携带 authorId，便于下游服务不查库完成 feed 分发。
     * 输入：ArticlePublishedEvent，包含文章 ID、作者 ID 和发生时间。
     * 输出：断言保存到仓储的 payload 中包含 authorId。
     */
    @Test
    void publishArticleEventShouldIncludeAuthorIdInPayload() {
        CapturingRepository repository = new CapturingRepository();
        LoggingDomainEventPublisher publisher = new LoggingDomainEventPublisher(
                repository,
                new FixedIdGenerator(),
                new TimeProvider(Clock.fixed(Instant.parse("2026-04-27T10:00:00Z"), ZoneOffset.UTC))
        );

        publisher.publish(new ArticlePublishedEvent(
                "1001",
                101L,
                OffsetDateTime.parse("2026-04-27T18:00:00+08:00")
        ));

        assertThat(repository.events).hasSize(1);
        assertThat(repository.events.get(0).payload()).contains("\"authorId\":101");
    }

    private static class FixedIdGenerator implements IdGenerator {

        @Override
        public long nextId() {
            return 9001L;
        }
    }

    private static class CapturingRepository implements EventOutboxRepository {

        private final List<EventOutboxDO> events = new ArrayList<>();

        @Override
        public void add(EventOutboxDO event) {
            events.add(event);
        }

        @Override
        public List<EventOutboxDO> claimPending(LocalDateTime now, String workerId, LocalDateTime lockedUntil, int limit) {
            return List.of();
        }

        @Override
        public void markSent(long id) {
        }

        @Override
        public void markFailed(long id, LocalDateTime nextRetryAt, String lastError) {
        }

        @Override
        public void markDead(long id, String lastError) {
        }
    }
}
