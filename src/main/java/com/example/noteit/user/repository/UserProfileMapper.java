package com.example.noteit.user.repository;

import com.example.noteit.user.model.UserProfileDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileMapper {

    UserProfileDO findById(@Param("id") long id);

    int insert(UserProfileDO userProfile);

    int updateBasicProfile(
            @Param("userId") long userId,
            @Param("nickname") String nickname,
            @Param("avatarUrl") String avatarUrl,
            @Param("bio") String bio
    );

    int incrementArticleCount(
            @Param("userId") long userId,
            @Param("delta") long delta
    );

    int incrementFollowerCount(
            @Param("userId") long userId,
            @Param("delta") long delta
    );

    int incrementFollowingCount(
            @Param("userId") long userId,
            @Param("delta") long delta
    );
}
