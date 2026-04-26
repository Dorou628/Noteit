package com.example.noteit.article.domain;

import com.example.noteit.article.model.FeedTimelineEntryDO;

import java.util.List;
import java.util.Optional;

public interface FeedTimelineCache {

    Optional<CachedTimelinePage> findInboxPage(long userId, int offset, int limit);

    Optional<List<FeedTimelineEntryDO>> findOutboxEntries(long authorId, int limit);

    void rebuildInbox(long userId, List<FeedTimelineEntryDO> entries, long total);

    void rebuildOutbox(long authorId, List<FeedTimelineEntryDO> entries, long total);

    void addInboxEntryIfCached(FeedTimelineEntryDO entry);

    void addOutboxEntryIfCached(FeedTimelineEntryDO entry);

    void evictInbox(long userId);

    void evictOutbox(long authorId);
}
