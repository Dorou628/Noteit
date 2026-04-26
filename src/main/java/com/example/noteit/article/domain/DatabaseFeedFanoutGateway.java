package com.example.noteit.article.domain;

import com.example.noteit.article.model.ArticleDO;
import com.example.noteit.article.model.FeedTimelineEntryDO;
import com.example.noteit.article.repository.ArticleRepository;
import com.example.noteit.article.repository.FeedTimelineRepository;
import com.example.noteit.common.id.IdGenerator;
import com.example.noteit.relation.repository.RelationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DatabaseFeedFanoutGateway implements FeedFanoutGateway {

    private static final int FOLLOW_BACKFILL_LIMIT = 100;

    private final IdGenerator idGenerator;
    private final ArticleRepository articleRepository;
    private final RelationRepository relationRepository;
    private final FeedTimelineRepository feedTimelineRepository;
    private final FeedTimelineCache feedTimelineCache;
    private final int feedCacheRebuildLimit;

    public DatabaseFeedFanoutGateway(
            IdGenerator idGenerator,
            ArticleRepository articleRepository,
            RelationRepository relationRepository,
            FeedTimelineRepository feedTimelineRepository,
            FeedTimelineCache feedTimelineCache,
            @Value("${noteit.feed.cache.rebuild-limit:1000}") int feedCacheRebuildLimit
    ) {
        this.idGenerator = idGenerator;
        this.articleRepository = articleRepository;
        this.relationRepository = relationRepository;
        this.feedTimelineRepository = feedTimelineRepository;
        this.feedTimelineCache = feedTimelineCache;
        this.feedCacheRebuildLimit = feedCacheRebuildLimit;
    }

    @Override
    @Transactional
    public void onArticlePublished(Long authorId, Long articleId) {
        ArticleDO article = articleRepository.findArticleById(articleId).orElse(null);
        if (article == null || article.status() != 1) {
            return;
        }

        FeedTimelineEntryDO outboxEntry = new FeedTimelineEntryDO(
                idGenerator.nextId(),
                null,
                authorId,
                articleId,
                article.publishedAt(),
                null
        );
        feedTimelineRepository.addOutboxEntry(outboxEntry);
        feedTimelineCache.addOutboxEntryIfCached(outboxEntry);

        List<Long> followerIds = relationRepository.findActiveFollowerIds(authorId);
        for (Long followerId : followerIds) {
            addInboxEntry(followerId, authorId, articleId, article.publishedAt());
        }
    }

    @Override
    @Transactional
    public void onArticleDeleted(Long authorId, Long articleId) {
        feedTimelineRepository.removeTimelineEntriesByArticle(articleId);
        feedTimelineCache.evictOutbox(authorId);
        List<Long> followerIds = relationRepository.findActiveFollowerIds(authorId);
        for (Long followerId : followerIds) {
            feedTimelineCache.evictInbox(followerId);
        }
    }

    @Override
    @Transactional
    public void onFollowRelationshipChanged(Long followerUserId, Long followeeUserId, boolean following) {
        if (!following) {
            feedTimelineRepository.removeInboxEntriesByAuthor(followerUserId, followeeUserId);
            feedTimelineCache.evictInbox(followerUserId);
            return;
        }

        List<FeedTimelineEntryDO> recentOutboxEntries = findRecentOutboxEntries(followeeUserId);
        for (FeedTimelineEntryDO entry : recentOutboxEntries) {
            addInboxEntry(followerUserId, followeeUserId, entry.articleId(), entry.publishedAt());
        }
    }

    private List<FeedTimelineEntryDO> findRecentOutboxEntries(Long authorId) {
        return feedTimelineCache.findOutboxEntries(authorId, FOLLOW_BACKFILL_LIMIT)
                .orElseGet(() -> rebuildAndReadOutbox(authorId));
    }

    private List<FeedTimelineEntryDO> rebuildAndReadOutbox(Long authorId) {
        int rebuildLimit = Math.max(feedCacheRebuildLimit, FOLLOW_BACKFILL_LIMIT);
        List<FeedTimelineEntryDO> entries = feedTimelineRepository.findRecentOutboxEntries(authorId, rebuildLimit);
        long total = feedTimelineRepository.countOutboxEntries(authorId);
        feedTimelineCache.rebuildOutbox(authorId, entries, total);
        if (entries.size() <= FOLLOW_BACKFILL_LIMIT) {
            return entries;
        }
        return entries.subList(0, FOLLOW_BACKFILL_LIMIT);
    }

    private void addInboxEntry(Long userId, Long authorId, Long articleId, java.time.LocalDateTime publishedAt) {
        FeedTimelineEntryDO inboxEntry = new FeedTimelineEntryDO(
                idGenerator.nextId(),
                userId,
                authorId,
                articleId,
                publishedAt,
                null
        );
        feedTimelineRepository.addInboxEntry(inboxEntry);
        feedTimelineCache.addInboxEntryIfCached(inboxEntry);
    }
}
