package com.example.noteit.common.id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class LocalIdGenerator implements IdGenerator {

    private static final long DEFAULT_INITIAL_VALUE = 10_000L;
    private static final String MAX_ID_SQL = """
            SELECT COALESCE(MAX(max_id), 0)
            FROM (
                SELECT MAX(id) AS max_id FROM article
                UNION ALL
                SELECT MAX(id) AS max_id FROM article_media
                UNION ALL
                SELECT MAX(id) AS max_id FROM article_like
                UNION ALL
                SELECT MAX(id) AS max_id FROM article_favorite
                UNION ALL
                SELECT MAX(id) AS max_id FROM user_follow
                UNION ALL
                SELECT MAX(id) AS max_id FROM article_outbox
                UNION ALL
                SELECT MAX(id) AS max_id FROM user_inbox
                UNION ALL
                SELECT MAX(id) AS max_id FROM event_outbox
                UNION ALL
                SELECT MAX(id) AS max_id FROM upload_task
            ) max_ids
            """;

    private static final AtomicLong SEQUENCE = new AtomicLong(DEFAULT_INITIAL_VALUE);

    private final JdbcTemplate jdbcTemplate;

    public LocalIdGenerator() {
        this.jdbcTemplate = null;
    }

    @Autowired
    public LocalIdGenerator(JdbcTemplate jdbcTemplate) {
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
        return SEQUENCE.incrementAndGet();
    }

    void syncToAtLeast(long persistedMaxId) {
        SEQUENCE.updateAndGet(current -> Math.max(current, persistedMaxId));
    }
}
