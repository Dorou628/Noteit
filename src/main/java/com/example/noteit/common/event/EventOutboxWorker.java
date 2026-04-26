package com.example.noteit.common.event;

import com.example.noteit.article.domain.FeedFanoutGateway;
import com.example.noteit.article.model.ArticleDO;
import com.example.noteit.article.repository.ArticleRepository;
import com.example.noteit.common.util.TimeProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventOutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(EventOutboxWorker.class);

    private final EventOutboxRepository eventOutboxRepository;
    private final FeedFanoutGateway feedFanoutGateway;
    private final ArticleRepository articleRepository;
    private final ObjectMapper objectMapper;
    private final TimeProvider timeProvider;
    private final boolean enabled;
    private final int batchSize;

    public EventOutboxWorker(
            EventOutboxRepository eventOutboxRepository,
            FeedFanoutGateway feedFanoutGateway,
            ArticleRepository articleRepository,
            TimeProvider timeProvider,
            @Value("${noteit.event-outbox.worker.enabled:true}") boolean enabled,
            @Value("${noteit.event-outbox.worker.batch-size:50}") int batchSize
    ) {
        this.eventOutboxRepository = eventOutboxRepository;
        this.feedFanoutGateway = feedFanoutGateway;
        this.articleRepository = articleRepository;
        this.objectMapper = new ObjectMapper();
        this.timeProvider = timeProvider;
        this.enabled = enabled;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${noteit.event-outbox.worker.fixed-delay:1000}")
    public void consumePendingEvents() {
        if (!enabled) {
            return;
        }
        List<EventOutboxDO> events = eventOutboxRepository.findPending(
                timeProvider.now().toLocalDateTime(),
                Math.max(batchSize, 1)
        );
        for (EventOutboxDO event : events) {
            processOne(event);
        }
    }

    private void processOne(EventOutboxDO event) {
        try {
            switch (event.eventType()) {
                case "ArticlePublished" -> handleArticlePublished(event);
                case "ArticleDeleted" -> handleArticleDeleted(event);
                case "FollowRelationshipChanged" -> handleFollowRelationshipChanged(event);
                case "ArticleUpdated" -> {
                    // Article detail is loaded from MySQL, so timeline caches do not need work here.
                }
                default -> log.info("Ignore event outbox row with unsupported type={}", event.eventType());
            }
            eventOutboxRepository.markSent(event.id());
        } catch (RuntimeException ex) {
            LocalDateTime nextRetryAt = nextRetryAt(event.retryCount());
            eventOutboxRepository.markFailed(event.id(), nextRetryAt, abbreviate(ex.getMessage()));
            log.warn("Failed to process outbox event id={}, type={}, nextRetryAt={}",
                    event.id(), event.eventType(), nextRetryAt, ex);
        }
    }

    private void handleArticlePublished(EventOutboxDO event) {
        long articleId = Long.parseLong(event.aggregateId());
        ArticleDO article = articleRepository.findArticleById(articleId).orElse(null);
        if (article == null || article.status() != 1) {
            return;
        }
        feedFanoutGateway.onArticlePublished(article.authorId(), articleId);
    }

    private void handleArticleDeleted(EventOutboxDO event) {
        long articleId = Long.parseLong(event.aggregateId());
        ArticleDO article = articleRepository.findArticleById(articleId).orElse(null);
        if (article == null) {
            return;
        }
        feedFanoutGateway.onArticleDeleted(article.authorId(), articleId);
    }

    private void handleFollowRelationshipChanged(EventOutboxDO event) {
        JsonNode payload = readPayload(event.payload());
        long followerUserId = payload.path("followerUserId").asLong();
        long followeeUserId = payload.path("followeeUserId").asLong();
        boolean following = payload.path("following").asBoolean();
        feedFanoutGateway.onFollowRelationshipChanged(followerUserId, followeeUserId, following);
    }

    private JsonNode readPayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid event payload", ex);
        }
    }

    private LocalDateTime nextRetryAt(int retryCount) {
        long delaySeconds = Math.min(60L, 1L << Math.min(retryCount, 6));
        return timeProvider.now().toLocalDateTime().plusSeconds(delaySeconds);
    }

    private String abbreviate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }
}
