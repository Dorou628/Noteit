package com.example.noteit.user.repository;

import com.example.noteit.user.model.UserProfileDO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisUserProfileRepository implements UserProfileRepository {

    private final UserProfileMapper userProfileMapper;

    public MyBatisUserProfileRepository(UserProfileMapper userProfileMapper) {
        this.userProfileMapper = userProfileMapper;
    }

    @Override
    public Optional<UserProfileDO> findById(long userId) {
        return Optional.ofNullable(userProfileMapper.findById(userId));
    }

    @Override
    public void insert(UserProfileDO userProfile) {
        userProfileMapper.insert(userProfile);
    }

    @Override
    public boolean updateBasicProfile(long userId, String nickname, String avatarUrl, String bio) {
        return userProfileMapper.updateBasicProfile(userId, nickname, avatarUrl, bio) > 0;
    }

    @Override
    public void incrementArticleCount(long userId, long delta) {
        userProfileMapper.incrementArticleCount(userId, delta);
    }

    @Override
    public void incrementFollowerCount(long userId, long delta) {
        userProfileMapper.incrementFollowerCount(userId, delta);
    }

    @Override
    public void incrementFollowingCount(long userId, long delta) {
        userProfileMapper.incrementFollowingCount(userId, delta);
    }
}
