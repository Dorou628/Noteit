package com.example.noteit.relation.repository;

import com.example.noteit.relation.model.UserFollowDO;
import com.example.noteit.user.model.UserProfileDO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisRelationRepository implements RelationRepository {

    private final RelationMapper relationMapper;

    public MyBatisRelationRepository(RelationMapper relationMapper) {
        this.relationMapper = relationMapper;
    }

    @Override
    public Optional<UserFollowDO> findFollowRelation(long followerId, long followeeId) {
        return Optional.ofNullable(relationMapper.findFollowRelation(followerId, followeeId));
    }

    @Override
    public void insertFollowRelation(UserFollowDO follow) {
        relationMapper.insertFollowRelation(follow);
    }

    @Override
    public void updateFollowStatus(long relationId, int status) {
        relationMapper.updateFollowStatus(relationId, status);
    }

    @Override
    public boolean hasActiveFollow(long followerId, long followeeId) {
        return relationMapper.countActiveFollowRelation(followerId, followeeId) > 0;
    }

    @Override
    public List<UserProfileDO> findFollowingProfiles(long userId, int offset, int limit) {
        return relationMapper.findFollowingProfiles(userId, offset, limit);
    }

    @Override
    public long countFollowingProfiles(long userId) {
        return relationMapper.countFollowingProfiles(userId);
    }

    @Override
    public List<UserProfileDO> findFollowerProfiles(long userId, int offset, int limit) {
        return relationMapper.findFollowerProfiles(userId, offset, limit);
    }

    @Override
    public long countFollowerProfiles(long userId) {
        return relationMapper.countFollowerProfiles(userId);
    }
}
