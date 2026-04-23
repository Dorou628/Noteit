package com.example.noteit.article.domain;

import com.example.noteit.article.model.ArticleCardResponse;
import com.example.noteit.article.model.ArticleFeedQuery;
import com.example.noteit.common.response.PageResponse;

public interface FeedReadService {

    PageResponse<ArticleCardResponse> readHomeFeed(Long viewerUserId, ArticleFeedQuery query);

    PageResponse<ArticleCardResponse> readPublishedArticles(Long viewerUserId, String authorId, int pageNo, int pageSize);
}
