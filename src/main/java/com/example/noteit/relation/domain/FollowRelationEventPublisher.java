package com.example.noteit.relation.domain;

public interface FollowRelationEventPublisher {

    void publishFollowed(Long followerUserId, Long followeeUserId);

    void publishUnfollowed(Long followerUserId, Long followeeUserId);
}
