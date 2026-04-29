package com.example.noteit.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CanalEventOutboxConsumerTest {

    @Test
    void consumeShouldDispatchEventOutboxInsertAndMarkSent() {
        CapturingHandler handler = new CapturingHandler();
        FakeRepository repository = new FakeRepository();
        CanalEventOutboxConsumer consumer = new CanalEventOutboxConsumer(
                new ObjectMapper(),
                new EventOutboxHandlerInvoker(List.of(handler)),
                repository
        );

        consumer.consume("""
                {
                  "id": 1,
                  "database": "noteit",
                  "table": "event_outbox",
                  "type": "INSERT",
                  "isDdl": false,
                  "pkNames": ["id"],
                  "data": [{
                    "id": "11",
                    "event_id": "evt-11",
                    "event_type": "ArticlePublished",
                    "aggregate_type": "Article",
                    "aggregate_id": "1001",
                    "payload": "{}",
                    "status": "0",
                    "retry_count": "0",
                    "created_at": "2026-04-26 10:00:00",
                    "updated_at": "2026-04-26 10:00:00"
                  }]
                }
                """);

        assertThat(handler.events).hasSize(1);
        assertThat(handler.events.get(0).id()).isEqualTo(11L);
        assertThat(handler.events.get(0).eventType()).isEqualTo("ArticlePublished");
        assertThat(repository.sentIds).containsExactly(11L);
    }

    @Test
    void consumeShouldIgnoreNonOutboxMessage() {
        CapturingHandler handler = new CapturingHandler();
        FakeRepository repository = new FakeRepository();
        CanalEventOutboxConsumer consumer = new CanalEventOutboxConsumer(
                new ObjectMapper(),
                new EventOutboxHandlerInvoker(List.of(handler)),
                repository
        );

        consumer.consume("""
                {
                  "id": 6,
                  "database": "noteit",
                  "es": 1777213099000,
                  "gtid": "",
                  "table": "article_like",
                  "type": "UPDATE",
                  "isDdl": false,
                  "mysqlType": {"id": "bigint unsigned"},
                  "old": [{"status": "1"}],
                  "pkNames": ["id"],
                  "sql": "",
                  "sqlType": {"id": -5},
                  "ts": 1777213099624,
                  "data": [{
                    "id": "1985789123000056582",
                    "article_id": "1985789123000056578",
                    "user_id": "102",
                    "status": "0"
                  }]
                }
                """);

        assertThat(handler.events).isEmpty();
        assertThat(repository.sentIds).isEmpty();
    }

    private static class CapturingHandler implements EventOutboxHandler {

        private final List<EventOutboxDO> events = new ArrayList<>();

        @Override
        public boolean supports(String eventType) {
            return true;
        }

        @Override
        public void handle(EventOutboxDO event) {
            events.add(event);
        }
    }

    private static class FakeRepository implements EventOutboxRepository {

        private final List<Long> sentIds = new ArrayList<>();

        @Override
        public void add(EventOutboxDO event) {
        }

        @Override
        public List<EventOutboxDO> claimPending(LocalDateTime now, String workerId, LocalDateTime lockedUntil, int limit) {
            return List.of();
        }

        @Override
        public void markSent(long id) {
            sentIds.add(id);
        }

        @Override
        public void markFailed(long id, LocalDateTime nextRetryAt, String lastError) {
        }

        @Override
        public void markDead(long id, String lastError) {
        }
    }
}
