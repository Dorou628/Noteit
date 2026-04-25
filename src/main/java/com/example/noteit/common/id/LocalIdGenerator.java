package com.example.noteit.common.id;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class LocalIdGenerator implements IdGenerator {

    private static final long DEFAULT_INITIAL_VALUE = 10_000L;
    private static final String MAX_ID_SQL = """
            SELECT COALESCE(MAX(max_id), 0)
            FROM (
                SELECT MAX(id) AS max_id FROM article
                UNION ALL
                SELECT MAX(id) AS max_id FROM article_like
                UNION ALL
                SELECT MAX(id) AS max_id FROM article_favorite
                UNION ALL
                SELECT MAX(id) AS max_id FROM user_follow
                UNION ALL
                SELECT MAX(id) AS max_id FROM upload_task
            ) max_ids
            """;

    private final AtomicLong sequence;
    private final JdbcTemplate jdbcTemplate;

    public LocalIdGenerator() {
        this.sequence = new AtomicLong(DEFAULT_INITIAL_VALUE);
        this.jdbcTemplate = null;
    }

    @Autowired
    public LocalIdGenerator(JdbcTemplate jdbcTemplate) {
        this.sequence = new AtomicLong(DEFAULT_INITIAL_VALUE);
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncSequenceWithDatabase() {
        if (jdbcTemplate == null) {
            return;
        }
        Long persistedMaxId = jdbcTemplate.queryForObject(MAX_ID_SQL, Long.class);
        if (persistedMaxId != null) {
            syncToAtLeast(persistedMaxId);
        }
    }

    @Override
    public long nextId() {
        return sequence.incrementAndGet();
    }

    void syncToAtLeast(long persistedMaxId) {
        sequence.updateAndGet(current -> Math.max(current, persistedMaxId));
    }
}
