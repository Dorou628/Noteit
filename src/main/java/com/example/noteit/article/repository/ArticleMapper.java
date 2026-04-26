package com.example.noteit.article.repository;

import com.example.noteit.article.model.ArticleDO;
import com.example.noteit.article.model.ArticleDetailDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper {

    /**
     * 作用：插入文章主表记录。
     * 输入：article 为待保存的文章对象。
     * 输出：返回受影响行数。
     */
    int insert(ArticleDO article);

    /**
     * 作用：按 ID 查询文章主表记录。
     * 输入：id 为文章 ID。
     * 输出：返回文章对象；不存在时返回 null。
     */
    ArticleDO findById(@Param("id") long id);

    /**
     * 作用：按 ID 查询文章详情记录。
     * 输入：id 为文章 ID。
     * 输出：返回文章详情对象；不存在时返回 null。
     */
    ArticleDetailDO findDetailById(@Param("id") long id);

    List<ArticleDetailDO> findDetailsByIds(@Param("ids") List<Long> ids);

    /**
     * 作用：分页查询首页 Feed 文章。
     * 输入：authorId 为可选作者 ID，offset 为偏移量，limit 为每页数量。
     * 输出：返回文章详情列表，供服务层组装卡片响应。
     */
    List<ArticleDetailDO> findFeedArticles(
            @Param("authorId") Long authorId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 作用：统计首页 Feed 文章数量。
     * 输入：authorId 为可选作者 ID。
     * 输出：返回文章总数。
     */
    long countFeedArticles(@Param("authorId") Long authorId);

    /**
     * 作用：分页查询用户点赞过的文章。
     * 输入：userId 为用户 ID，offset 为偏移量，limit 为每页数量。
     * 输出：返回文章详情列表，供服务层组装卡片响应。
     */
    List<ArticleDetailDO> findLikedArticles(
            @Param("userId") long userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 作用：统计用户点赞过的文章数量。
     * 输入：userId 为用户 ID。
     * 输出：返回文章总数。
     */
    long countLikedArticles(@Param("userId") long userId);

    /**
     * 作用：分页查询用户收藏的文章。
     * 输入：userId 为用户 ID，offset 为偏移量，limit 为每页数量。
     * 输出：返回文章详情列表，供服务层组装卡片响应。
     */
    List<ArticleDetailDO> findFavoritedArticles(
            @Param("userId") long userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 作用：统计用户收藏的文章数量。
     * 输入：userId 为用户 ID。
     * 输出：返回文章总数。
     */
    long countFavoritedArticles(@Param("userId") long userId);

    /**
     * 作用：更新文章主表记录。
     * 输入：article 为更新后的文章对象。
     * 输出：返回受影响行数。
     */
    int update(ArticleDO article);

    int softDelete(
            @Param("id") long id,
            @Param("authorId") long authorId
    );
}
