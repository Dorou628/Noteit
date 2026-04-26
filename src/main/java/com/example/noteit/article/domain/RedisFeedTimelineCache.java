package com.example.noteit.article.domain;

import com.example.noteit.article.model.FeedTimelineEntryDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class RedisFeedTimelineCache implements FeedTimelineCache {

    private static final Logger log = LoggerFactory.getLogger(RedisFeedTimelineCache.class);
    private static final String INBOX_KEY_PREFIX = "noteit:feed:inbox:";
    private static final String OUTBOX_KEY_PREFIX = "noteit:feed:outbox:";
    private static final String TOTAL_SUFFIX = ":total";

    private final StringRedisTemplate redisTemplate;
    private final boolean enabled;
    private final Duration ttl;

    public RedisFeedTimelineCache(
            StringRedisTemplate redisTemplate,
            @Value("${noteit.feed.cache.enabled:false}") boolean enabled,
            @Value("${noteit.feed.cache.ttl:30m}") Duration ttl
    ) {
        this.redisTemplate = redisTemplate;
        this.enabled = enabled;
        this.ttl = ttl;
    }

    @Override
    public Optional<CachedTimelinePage> findInboxPage(long userId, int offset, int limit) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            String key = inboxKey(userId);
            String totalKey = totalKey(key);
            Boolean timelineExists = redisTemplate.hasKey(key);
            Boolean totalExists = redisTemplate.hasKey(totalKey);
            if (!Boolean.TRUE.equals(timelineExists) && !Boolean.TRUE.equals(totalExists)) {
                return Optional.empty();
            }

            Long cachedTotal = parseLong(redisTemplate.opsForValue().get(totalKey));
            if (!Boolean.TRUE.equals(timelineExists)) {
                if (cachedTotal != null && cachedTotal == 0L) {
                    redisTemplate.expire(totalKey, ttl);
                    return Optional.of(new CachedTimelinePage(List.of(), 0L));
                }
                return Optional.empty();
            }

            Long cachedSize = redisTemplate.opsForZSet().zCard(key);
            if (isOutsideCachedWindow(offset, limit, cachedSize, cachedTotal)) {
                return Optional.empty();
            }

            Set<String> values = redisTemplate.opsForZSet().reverseRange(key, offset, offset + limit - 1L);
            List<Long> articleIds = new ArrayList<>();
            if (values != null) {
                for (String value : values) {
                    articleIds.add(Long.parseLong(value));
                }
            }
            long total = cachedTotal == null ? articleIds.size() : cachedTotal;
            redisTemplate.expire(key, ttl);
            redisTemplate.expire(totalKey, ttl);
            return Optional.of(new CachedTimelinePage(articleIds, total));
        } catch (RuntimeException ex) {
            log.warn("Failed to read feed inbox cache for userId={}", userId, ex);
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<FeedTimelineEntryDO>> findOutboxEntries(long authorId, int limit) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            String key = outboxKey(authorId);
            String totalKey = totalKey(key);
            Boolean timelineExists = redisTemplate.hasKey(key);
            Boolean totalExists = redisTemplate.hasKey(totalKey);
            if (!Boolean.TRUE.equals(timelineExists) && !Boolean.TRUE.equals(totalExists)) {
                return Optional.empty();
            }

            Long cachedTotal = parseLong(redisTemplate.opsForValue().get(totalKey));
            if (!Boolean.TRUE.equals(timelineExists)) {
                if (cachedTotal != null && cachedTotal == 0L) {
                    redisTemplate.expire(totalKey, ttl);
                    return Optional.of(List.of());
                }
                return Optional.empty();
            }

            Long cachedSize = redisTemplate.opsForZSet().zCard(key);
            if (isOutsideCachedWindow(0, limit, cachedSize, cachedTotal)) {
                return Optional.empty();
            }

            Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> values =
                    redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1L);
            List<FeedTimelineEntryDO> entries = new ArrayList<>();
            if (values != null) {
                for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<String> value : values) {
                    if (value.getValue() == null || value.getScore() == null) {
                        continue;
                    }
                    Long articleId = parseLong(value.getValue());
                    if (articleId == null) {
                        continue;
                    }
                    entries.add(new FeedTimelineEntryDO(
                            0L,
                            null,
                            authorId,
                            articleId,
                            publishedAtFromScore(value.getScore()),
                            null
                    ));
                }
            }
            redisTemplate.expire(key, ttl);
            redisTemplate.expire(totalKey, ttl);
            return Optional.of(entries);
        } catch (RuntimeException ex) {
            log.warn("Failed to read feed outbox cache for authorId={}", authorId, ex);
            return Optional.empty();
        }
    }

    @Override
    public void rebuildInbox(long userId, List<FeedTimelineEntryDO> entries, long total) {
        if (!enabled) {
            return;
        }
        try {
            String key = inboxKey(userId);
            redisTemplate.delete(key);
            for (FeedTimelineEntryDO entry : entries) {
                redisTemplate.opsForZSet().add(key, String.valueOf(entry.articleId()), score(entry));
            }
            redisTemplate.opsForValue().set(totalKey(key), String.valueOf(total), ttl);
            redisTemplate.expire(key, ttl);
        } catch (RuntimeException ex) {
            log.warn("Failed to rebuild feed inbox cache for userId={}", userId, ex);
        }
    }

    @Override
    public void rebuildOutbox(long authorId, List<FeedTimelineEntryDO> entries, long total) {
        if (!enabled) {
            return;
        }
        try {
            String key = outboxKey(authorId);
            redisTemplate.delete(key);
            for (FeedTimelineEntryDO entry : entries) {
                redisTemplate.opsForZSet().add(key, String.valueOf(entry.articleId()), score(entry));
            }
            redisTemplate.opsForValue().set(totalKey(key), String.valueOf(total), ttl);
            redisTemplate.expire(key, ttl);
        } catch (RuntimeException ex) {
            log.warn("Failed to rebuild feed outbox cache for authorId={}", authorId, ex);
        }
    }

    @Override
    public void addInboxEntryIfCached(FeedTimelineEntryDO entry) {
        if (!enabled || entry.userId() == null) {
            return;
        }
        tryAddIfCached(inboxKey(entry.userId()), entry);
    }

    @Override
    public void addOutboxEntryIfCached(FeedTimelineEntryDO entry) {
        if (!enabled) {
            return;
        }
        tryAddIfCached(outboxKey(entry.authorId()), entry);
    }

    @Override
    public void evictInbox(long userId) {
        if (!enabled) {
            return;
        }
        try {
            String key = inboxKey(userId);
            redisTemplate.delete(key);
            redisTemplate.delete(totalKey(key));
        } catch (RuntimeException ex) {
            log.warn("Failed to evict feed inbox cache for userId={}", userId, ex);
        }
    }

    @Override
    public void evictOutbox(long authorId) {
        if (!enabled) {
            return;
        }
        try {
            String key = outboxKey(authorId);
            redisTemplate.delete(key);
            redisTemplate.delete(totalKey(key));
        } catch (RuntimeException ex) {
            log.warn("Failed to evict feed outbox cache for authorId={}", authorId, ex);
        }
    }

    private void tryAddIfCached(String key, FeedTimelineEntryDO entry) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            if (!Boolean.TRUE.equals(exists)) {
                return;
            }
            Boolean added = redisTemplate.opsForZSet().add(key, String.valueOf(entry.articleId()), score(entry));
            if (Boolean.TRUE.equals(added)) {
                redisTemplate.opsForValue().increment(totalKey(key));
            }
            redisTemplate.expire(key, ttl);
            redisTemplate.expire(totalKey(key), ttl);
        } catch (RuntimeException ex) {
            log.warn("Failed to update feed timeline cache key={}", key, ex);
        }
    }

    private boolean isOutsideCachedWindow(int offset, int limit, Long cachedSize, Long cachedTotal) {
        if (cachedSize == null || cachedTotal == null) {
            return false;
        }
        if (cachedTotal <= cachedSize) {
            return false;
        }
        return offset + (long) limit > cachedSize;
    }

    private LocalDateTime publishedAtFromScore(double score) {
        long publishedAtMillis = (long) Math.floor(score);
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(publishedAtMillis), ZoneId.systemDefault());
    }

    private double score(FeedTimelineEntryDO entry) {
        long publishedAtMillis = entry.publishedAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        return publishedAtMillis + (entry.articleId() % 1000) / 1000.0;
    }

    private Long parseLong(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String inboxKey(long userId) {
        return INBOX_KEY_PREFIX + userId;
    }

    private String outboxKey(long authorId) {
        return OUTBOX_KEY_PREFIX + authorId;
    }

    private String totalKey(String timelineKey) {
        return timelineKey + TOTAL_SUFFIX;
    }
}
