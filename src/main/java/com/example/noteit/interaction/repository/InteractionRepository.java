package com.example.noteit.interaction.repository;

import com.example.noteit.interaction.model.ArticleFavoriteDO;
import com.example.noteit.interaction.model.ArticleLikeDO;

import java.util.Optional;

public interface InteractionRepository {

    /**
     * 作用：按文章 ID 和用户 ID 查询点赞关系。
     * 输入：articleId 为文章 ID，userId 为用户 ID。
     * 输出：如果关系存在则返回点赞关系，否则返回空。
     */
    Optional<ArticleLikeDO> findLikeRelation(long articleId, long userId);

    /**
     * 作用：新增一条点赞关系记录。
     * 输入：articleLike 为待持久化的点赞关系对象。
     * 输出：无，写入数据库。
     */
    void insertLikeRelation(ArticleLikeDO articleLike);

    /**
     * 作用：更新已有点赞关系的状态。
     * 输入：relationId 为关系主键，status 为最新状态。
     * 输出：无，更新数据库中的点赞状态。
     */
    void updateLikeStatus(long relationId, int status);

    /**
     * 作用：统计文章当前处于生效状态的点赞数量。
     * 输入：articleId 为文章 ID。
     * 输出：返回点赞总数。
     */
    long countActiveLikes(long articleId);

    /**
     * 作用：判断某个用户当前是否点赞了指定文章。
     * 输入：articleId 为文章 ID，userId 为用户 ID。
     * 输出：true 表示已点赞，false 表示未点赞。
     */
    boolean hasActiveLike(long articleId, long userId);

    /**
     * 作用：按文章 ID 和用户 ID 查询收藏关系。
     * 输入：articleId 为文章 ID，userId 为用户 ID。
     * 输出：如果关系存在则返回收藏关系，否则返回空。
     */
    Optional<ArticleFavoriteDO> findFavoriteRelation(long articleId, long userId);

    /**
     * 作用：新增一条收藏关系记录。
     * 输入：articleFavorite 为待持久化的收藏关系对象。
     * 输出：无，写入数据库。
     */
    void insertFavoriteRelation(ArticleFavoriteDO articleFavorite);

    /**
     * 作用：更新已有收藏关系的状态。
     * 输入：relationId 为关系主键，status 为最新状态。
     * 输出：无，更新数据库中的收藏状态。
     */
    void updateFavoriteStatus(long relationId, int status);

    /**
     * 作用：统计文章当前处于生效状态的收藏数量。
     * 输入：articleId 为文章 ID。
     * 输出：返回收藏总数。
     */
    long countActiveFavorites(long articleId);

    /**
     * 作用：判断某个用户当前是否收藏了指定文章。
     * 输入：articleId 为文章 ID，userId 为用户 ID。
     * 输出：true 表示已收藏，false 表示未收藏。
     */
    boolean hasActiveFavorite(long articleId, long userId);
}
