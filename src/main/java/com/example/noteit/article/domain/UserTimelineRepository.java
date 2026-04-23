package com.example.noteit.article.domain;

import java.util.List;

public interface UserTimelineRepository {

    List<Long> findInboxArticleIds(Long viewerUserId, Long cursorArticleId, int limit);

    List<Long> findOutboxArticleIds(Long authorId, Long cursorArticleId, int limit);
}
