package com.example.noteit.relation.repository;

import com.example.noteit.relation.model.UserFollowDO;
import com.example.noteit.user.model.UserProfileDO;

import java.util.List;
import java.util.Optional;

public interface RelationRepository {

    Optional<UserFollowDO> findFollowRelation(long followerId, long followeeId);

    void insertFollowRelation(UserFollowDO follow);

    void updateFollowStatus(long relationId, int status);

    boolean hasActiveFollow(long followerId, long followeeId);

    List<UserProfileDO> findFollowingProfiles(long userId, int offset, int limit);

    long countFollowingProfiles(long userId);

    List<UserProfileDO> findFollowerProfiles(long userId, int offset, int limit);

    long countFollowerProfiles(long userId);

    List<Long> findActiveFollowerIds(long followeeId);
}
