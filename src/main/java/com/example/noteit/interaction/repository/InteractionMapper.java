package com.example.noteit.interaction.repository;

import com.example.noteit.interaction.model.ArticleFavoriteDO;
import com.example.noteit.interaction.model.ArticleLikeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InteractionMapper {

    /**
     * 作用：查询指定用户在指定文章上的点赞关系。
     * 输入：articleId 为文章 ID，userId 为用户 ID。
     * 输出：返回点赞关系；若不存在则返回 null。
     */
    ArticleLikeDO findLikeRelation(@Param("articleId") long articleId, @Param("userId") long userId);

    /**
     * 作用：插入新的点赞关系。
     * 输入：articleLike 为点赞关系对象。
     * 输出：返回受影响行数。
     */
    int insertLikeRelation(ArticleLikeDO articleLike);

    /**
     * 作用：更新点赞关系状态。
     * 输入：id 为点赞关系主键，status 为目标状态。
     * 输出：返回受影响行数。
     */
    int updateLikeStatus(@Param("id") long id, @Param("status") int status);

    /**
     * 作用：统计文章当前生效的点赞数。
     * 输入：articleId 为文章 ID。
     * 输出：返回点赞总数。
     */
    long countActiveLikes(@Param("articleId") long articleId);

    /**
     * 作用：判断当前用户是否已点赞文章。
     * 输入：articleId 为文章 ID，userId 为用户 ID。
     * 输出：返回命中数量，0 表示未点赞。
     */
    int countActiveLikeRelation(@Param("articleId") long articleId, @Param("userId") long userId);

    /**
     * 作用：查询指定用户在指定文章上的收藏关系。
     * 输入：articleId 为文章 ID，userId 为用户 ID。
     * 输出：返回收藏关系；若不存在则返回 null。
     */
    ArticleFavoriteDO findFavoriteRelation(@Param("articleId") long articleId, @Param("userId") long userId);

    /**
     * 作用：插入新的收藏关系。
     * 输入：articleFavorite 为收藏关系对象。
     * 输出：返回受影响行数。
     */
    int insertFavoriteRelation(ArticleFavoriteDO articleFavorite);

    /**
     * 作用：更新收藏关系状态。
     * 输入：id 为收藏关系主键，status 为目标状态。
     * 输出：返回受影响行数。
     */
    int updateFavoriteStatus(@Param("id") long id, @Param("status") int status);

    /**
     * 作用：统计文章当前生效的收藏数。
     * 输入：articleId 为文章 ID。
     * 输出：返回收藏总数。
     */
    long countActiveFavorites(@Param("articleId") long articleId);

    /**
     * 作用：判断当前用户是否已收藏文章。
     * 输入：articleId 为文章 ID，userId 为用户 ID。
     * 输出：返回命中数量，0 表示未收藏。
     */
    int countActiveFavoriteRelation(@Param("articleId") long articleId, @Param("userId") long userId);
}
