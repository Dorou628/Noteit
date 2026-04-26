package com.example.noteit.relation.repository;

import com.example.noteit.relation.model.UserFollowDO;
import com.example.noteit.user.model.UserProfileDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RelationMapper {

    UserFollowDO findFollowRelation(
            @Param("followerId") long followerId,
            @Param("followeeId") long followeeId
    );

    int insertFollowRelation(UserFollowDO follow);

    int updateFollowStatus(@Param("id") long id, @Param("status") int status);

    int countActiveFollowRelation(
            @Param("followerId") long followerId,
            @Param("followeeId") long followeeId
    );

    List<UserProfileDO> findFollowingProfiles(
            @Param("userId") long userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countFollowingProfiles(@Param("userId") long userId);

    List<UserProfileDO> findFollowerProfiles(
            @Param("userId") long userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countFollowerProfiles(@Param("userId") long userId);

    List<Long> findActiveFollowerIds(@Param("followeeId") long followeeId);
}
