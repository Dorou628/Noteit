package com.example.noteit.article.repository;

import com.example.noteit.article.model.ArticleDetailDO;
import com.example.noteit.article.model.FeedTimelineEntryDO;

import java.util.List;

public interface FeedTimelineRepository {

    void addOutboxEntry(FeedTimelineEntryDO entry);

    void addInboxEntry(FeedTimelineEntryDO entry);

    List<FeedTimelineEntryDO> findRecentOutboxEntries(long authorId, int limit);

    long countOutboxEntries(long authorId);

    List<FeedTimelineEntryDO> findRecentInboxEntries(long userId, int limit);

    List<ArticleDetailDO> findInboxArticles(long userId, int offset, int limit);

    long countInboxArticles(long userId);

    void removeInboxEntriesByAuthor(long userId, long authorId);

    void removeTimelineEntriesByArticle(long articleId);
}
