package com.example.noteit.common.event;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MyBatisEventOutboxRepository implements EventOutboxRepository {

    private final EventOutboxMapper eventOutboxMapper;

    public MyBatisEventOutboxRepository(EventOutboxMapper eventOutboxMapper) {
        this.eventOutboxMapper = eventOutboxMapper;
    }

    @Override
    public void add(EventOutboxDO event) {
        eventOutboxMapper.insert(event);
    }

    @Override
    public List<EventOutboxDO> claimPending(LocalDateTime now, String workerId, LocalDateTime lockedUntil, int limit) {
        return eventOutboxMapper.findClaimCandidates(now, limit)
                .stream()
                .filter(event -> eventOutboxMapper.claim(event.id(), now, workerId, lockedUntil) > 0)
                .map(event -> eventOutboxMapper.findById(event.id()))
                .toList();
    }

    @Override
    public void markSent(long id) {
        eventOutboxMapper.markSent(id);
    }

    @Override
    public void markFailed(long id, LocalDateTime nextRetryAt, String lastError) {
        eventOutboxMapper.markFailed(id, nextRetryAt, lastError);
    }

    @Override
    public void markDead(long id, String lastError) {
        eventOutboxMapper.markDead(id, lastError);
    }
}
