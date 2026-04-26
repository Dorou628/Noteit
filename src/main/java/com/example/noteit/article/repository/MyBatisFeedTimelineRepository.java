package com.example.noteit.article.repository;

import com.example.noteit.article.model.ArticleDetailDO;
import com.example.noteit.article.model.FeedTimelineEntryDO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MyBatisFeedTimelineRepository implements FeedTimelineRepository {

    private final FeedTimelineMapper feedTimelineMapper;

    public MyBatisFeedTimelineRepository(FeedTimelineMapper feedTimelineMapper) {
        this.feedTimelineMapper = feedTimelineMapper;
    }

    @Override
    public void addOutboxEntry(FeedTimelineEntryDO entry) {
        feedTimelineMapper.insertOutboxIfAbsent(entry);
    }

    @Override
    public void addInboxEntry(FeedTimelineEntryDO entry) {
        feedTimelineMapper.insertInboxIfAbsent(entry);
    }

    @Override
    public List<FeedTimelineEntryDO> findRecentOutboxEntries(long authorId, int limit) {
        return feedTimelineMapper.findRecentOutboxEntries(authorId, limit);
    }

    @Override
    public long countOutboxEntries(long authorId) {
        return feedTimelineMapper.countOutboxEntries(authorId);
    }

    @Override
    public List<FeedTimelineEntryDO> findRecentInboxEntries(long userId, int limit) {
        return feedTimelineMapper.findRecentInboxEntries(userId, limit);
    }

    @Override
    public List<ArticleDetailDO> findInboxArticles(long userId, int offset, int limit) {
        return feedTimelineMapper.findInboxArticles(userId, offset, limit);
    }

    @Override
    public long countInboxArticles(long userId) {
        return feedTimelineMapper.countInboxArticles(userId);
    }

    @Override
    public void removeInboxEntriesByAuthor(long userId, long authorId) {
        feedTimelineMapper.deleteInboxByAuthor(userId, authorId);
    }

    @Override
    public void removeTimelineEntriesByArticle(long articleId) {
        feedTimelineMapper.deleteInboxByArticle(articleId);
        feedTimelineMapper.deleteOutboxByArticle(articleId);
    }
}
