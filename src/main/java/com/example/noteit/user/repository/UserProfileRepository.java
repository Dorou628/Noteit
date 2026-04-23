package com.example.noteit.user.repository;

import com.example.noteit.user.model.UserProfileDO;

import java.util.Optional;

public interface UserProfileRepository {

    Optional<UserProfileDO> findById(long userId);

    void insert(UserProfileDO userProfile);

    boolean updateBasicProfile(long userId, String nickname, String avatarUrl, String bio);

    void incrementArticleCount(long userId, long delta);

    void incrementFollowerCount(long userId, long delta);

    void incrementFollowingCount(long userId, long delta);
}
