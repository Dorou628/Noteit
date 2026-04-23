package com.example.noteit.interaction.repository;

import com.example.noteit.interaction.model.ArticleFavoriteDO;
import com.example.noteit.interaction.model.ArticleLikeDO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisInteractionRepository implements InteractionRepository {

    private final InteractionMapper interactionMapper;

    public MyBatisInteractionRepository(InteractionMapper interactionMapper) {
        this.interactionMapper = interactionMapper;
    }

    /**
     * 作用：按文章和用户查询点赞关系。
     * 输入：articleId 为文章 ID，userId 为用户 ID。
     * 输出：返回点赞关系，找不到则返回空。
     */
    @Override
    public Optional<ArticleLikeDO> findLikeRelation(long articleId, long userId) {
        return Optional.ofNullable(interactionMapper.findLikeRelation(articleId, userId));
    }

    /**
     * 作用：新增点赞关系。
     * 输入：articleLike 为待保存的点赞关系。
     * 输出：无。
     */
    @Override
    public void insertLikeRelation(ArticleLikeDO articleLike) {
        interactionMapper.insertLikeRelation(articleLike);
    }

    /**
     * 作用：更新点赞状态。
     * 输入：relationId 为点赞关系主键，status 为目标状态。
     * 输出：无。
     */
    @Override
    public void updateLikeStatus(long relationId, int status) {
        interactionMapper.updateLikeStatus(relationId, status);
    }

    /**
     * 作用：统计文章生效中的点赞数量。
     * 输入：articleId 为文章 ID。
     * 输出：返回点赞总数。
     */
    @Override
    public long countActiveLikes(long articleId) {
        return interactionMapper.countActiveLikes(articleId);
    }

    /**
     * 作用：判断用户是否已点赞文章。
     * 输入：articleId 为文章 ID，userId 为用户 ID。
     * 输出：true 表示已点赞，false 表示未点赞。
     */
    @Override
    public boolean hasActiveLike(long articleId, long userId) {
        return interactionMapper.countActiveLikeRelation(articleId, userId) > 0;
    }

    /**
     * 作用：按文章和用户查询收藏关系。
     * 输入：articleId 为文章 ID，userId 为用户 ID。
     * 输出：返回收藏关系，找不到则返回空。
     */
    @Override
    public Optional<ArticleFavoriteDO> findFavoriteRelation(long articleId, long userId) {
        return Optional.ofNullable(interactionMapper.findFavoriteRelation(articleId, userId));
    }

    /**
     * 作用：新增收藏关系。
     * 输入：articleFavorite 为待保存的收藏关系。
     * 输出：无。
     */
    @Override
    public void insertFavoriteRelation(ArticleFavoriteDO articleFavorite) {
        interactionMapper.insertFavoriteRelation(articleFavorite);
    }

    /**
     * 作用：更新收藏状态。
     * 输入：relationId 为收藏关系主键，status 为目标状态。
     * 输出：无。
     */
    @Override
    public void updateFavoriteStatus(long relationId, int status) {
        interactionMapper.updateFavoriteStatus(relationId, status);
    }

    /**
     * 作用：统计文章生效中的收藏数量。
     * 输入：articleId 为文章 ID。
     * 输出：返回收藏总数。
     */
    @Override
    public long countActiveFavorites(long articleId) {
        return interactionMapper.countActiveFavorites(articleId);
    }

    /**
     * 作用：判断用户是否已收藏文章。
     * 输入：articleId 为文章 ID，userId 为用户 ID。
     * 输出：true 表示已收藏，false 表示未收藏。
     */
    @Override
    public boolean hasActiveFavorite(long articleId, long userId) {
        return interactionMapper.countActiveFavoriteRelation(articleId, userId) > 0;
    }
}
