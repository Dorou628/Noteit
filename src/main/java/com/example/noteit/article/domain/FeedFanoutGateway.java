package com.example.noteit.article.domain;

public interface FeedFanoutGateway {

    void onArticlePublished(Long authorId, Long articleId);

    void onFollowRelationshipChanged(Long followerUserId, Long followeeUserId, boolean following);
}
