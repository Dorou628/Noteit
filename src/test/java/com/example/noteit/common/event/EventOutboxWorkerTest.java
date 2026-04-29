package com.example.noteit.common.event;

import com.example.noteit.common.util.TimeProvider;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventOutboxWorkerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 26, 9, 0);

    private final TimeProvider timeProvider = new TimeProvider(
            Clock.fixed(Instant.parse("2026-04-26T09:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void consumePendingEventsShouldClaimWithLockWindow() {
        FakeRepository repository = new FakeRepository(List.of(event(1L, "UnsupportedEvent", 0)));
        EventOutboxWorker worker = new EventOutboxWorker(
                repository,
                new LocalEventOutboxDispatcher(new EventOutboxHandlerInvoker(List.of())),
                timeProvider,
                true,
                25,
                3,
                45
        );

        worker.consumePendingEvents();

        assertThat(repository.claimNow).isEqualTo(NOW);
        assertThat(repository.claimLockedUntil).isEqualTo(NOW.plusSeconds(45));
        assertThat(repository.claimLimit).isEqualTo(25);
        assertThat(repository.claimWorkerId).isNotBlank();
        assertThat(repository.sentIds).containsExactly(1L);
    }

    @Test
    void consumePendingEventsShouldRetryFailedEventBeforeMaxRetries() {
        FakeRepository repository = new FakeRepository(List.of(event(2L, "FailingEvent", 0)));
        EventOutboxWorker worker = new EventOutboxWorker(
                repository,
                new LocalEventOutboxDispatcher(new EventOutboxHandlerInvoker(List.of(new FailingHandler()))),
                timeProvider,
                true,
                10,
                3,
                30
        );

        worker.consumePendingEvents();

        assertThat(repository.failedIds).containsExactly(2L);
        assertThat(repository.failedNextRetryAt).isEqualTo(NOW.plusSeconds(1));
        assertThat(repository.deadIds).isEmpty();
    }

    @Test
    void consumePendingEventsShouldMoveEventToDeadWhenRetryLimitReached() {
        FakeRepository repository = new FakeRepository(List.of(event(3L, "FailingEvent", 2)));
        EventOutboxWorker worker = new EventOutboxWorker(
                repository,
                new LocalEventOutboxDispatcher(new EventOutboxHandlerInvoker(List.of(new FailingHandler()))),
                timeProvider,
                true,
                10,
                3,
                30
        );

        worker.consumePendingEvents();

        assertThat(repository.deadIds).containsExactly(3L);
        assertThat(repository.failedIds).isEmpty();
    }

    private static EventOutboxDO event(long id, String eventType, int retryCount) {
        return new EventOutboxDO(
                id,
                String.valueOf(id),
                eventType,
                "Article",
                String.valueOf(id),
                "{}",
                EventOutboxStatus.PROCESSING,
                retryCount,
                null,
                "worker",
                NOW.plusSeconds(30),
                null,
                NOW,
                NOW
        );
    }

    private static class FailingHandler implements EventOutboxHandler {

        @Override
        public boolean supports(String eventType) {
            return "FailingEvent".equals(eventType);
        }

        @Override
        public void handle(EventOutboxDO event) {
            throw new IllegalStateException("planned failure");
        }
    }

    private static class FakeRepository implements EventOutboxRepository {

        private final List<EventOutboxDO> events;
        private final List<Long> sentIds = new ArrayList<>();
        private final List<Long> failedIds = new ArrayList<>();
        private final List<Long> deadIds = new ArrayList<>();
        private LocalDateTime claimNow;
        private String claimWorkerId;
        private LocalDateTime claimLockedUntil;
        private int claimLimit;
        private LocalDateTime failedNextRetryAt;

        FakeRepository(List<EventOutboxDO> events) {
            this.events = events;
        }

        @Override
        public void add(EventOutboxDO event) {
        }

        @Override
        public List<EventOutboxDO> claimPending(LocalDateTime now, String workerId, LocalDateTime lockedUntil, int limit) {
            this.claimNow = now;
            this.claimWorkerId = workerId;
            this.claimLockedUntil = lockedUntil;
            this.claimLimit = limit;
            return events;
        }

        @Override
        public void markSent(long id) {
            sentIds.add(id);
        }

        @Override
        public void markFailed(long id, LocalDateTime nextRetryAt, String lastError) {
            failedIds.add(id);
            failedNextRetryAt = nextRetryAt;
        }

        @Override
        public void markDead(long id, String lastError) {
            deadIds.add(id);
        }
    }
}
