package com.example.noteit.common.event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventOutboxRepository {

    void add(EventOutboxDO event);

    List<EventOutboxDO> claimPending(LocalDateTime now, String workerId, LocalDateTime lockedUntil, int limit);

    void markSent(long id);

    void markFailed(long id, LocalDateTime nextRetryAt, String lastError);

    void markDead(long id, String lastError);
}
