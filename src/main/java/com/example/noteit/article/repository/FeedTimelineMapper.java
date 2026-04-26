package com.example.noteit.article.repository;

import com.example.noteit.article.model.ArticleDetailDO;
import com.example.noteit.article.model.FeedTimelineEntryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FeedTimelineMapper {

    int insertOutboxIfAbsent(FeedTimelineEntryDO entry);

    int insertInboxIfAbsent(FeedTimelineEntryDO entry);

    List<FeedTimelineEntryDO> findRecentOutboxEntries(
            @Param("authorId") long authorId,
            @Param("limit") int limit
    );

    long countOutboxEntries(@Param("authorId") long authorId);

    List<FeedTimelineEntryDO> findRecentInboxEntries(
            @Param("userId") long userId,
            @Param("limit") int limit
    );

    List<ArticleDetailDO> findInboxArticles(
            @Param("userId") long userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countInboxArticles(@Param("userId") long userId);

    int deleteInboxByAuthor(
            @Param("userId") long userId,
            @Param("authorId") long authorId
    );

    int deleteOutboxByArticle(@Param("articleId") long articleId);

    int deleteInboxByArticle(@Param("articleId") long articleId);
}
