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
    public List<EventOutboxDO> findPending(LocalDateTime now, int limit) {
        return eventOutboxMapper.findPending(now, limit);
    }

    @Override
    public void markSent(long id) {
        eventOutboxMapper.markSent(id);
    }

    @Override
    public void markFailed(long id, LocalDateTime nextRetryAt, String lastError) {
        eventOutboxMapper.markFailed(id, nextRetryAt, lastError);
    }
}
