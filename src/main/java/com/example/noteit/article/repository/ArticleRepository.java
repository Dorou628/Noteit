package com.example.noteit.article.repository;

import com.example.noteit.article.model.ArticleDO;
import com.example.noteit.article.model.ArticleDetailDO;
import com.example.noteit.article.model.ArticleMediaDO;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {

    /**
     * 作用：保存文章主表数据。
     * 输入：article 为待保存的文章对象。
     * 输出：无，写入数据库。
     */
    void insertArticle(ArticleDO article);

    /**
     * 作用：批量保存文章图片数据。
     * 输入：mediaList 为文章图片列表。
     * 输出：无，写入数据库。
     */
    void insertMedia(List<ArticleMediaDO> mediaList);

    /**
     * 作用：按文章 ID 查询文章主表数据。
     * 输入：articleId 为文章 ID。
     * 输出：返回文章对象，找不到则返回空。
     */
    Optional<ArticleDO> findArticleById(long articleId);

    /**
     * 作用：按文章 ID 查询文章详情数据。
     * 输入：articleId 为文章 ID。
     * 输出：返回文章详情对象，找不到则返回空。
     */
    Optional<ArticleDetailDO> findDetailById(long articleId);

    List<ArticleDetailDO> findDetailsByIds(List<Long> articleIds);

    /**
     * 作用：查询首页 Feed 卡片数据。
     * 输入：authorId 为可选作者 ID，offset 为偏移量，limit 为每页数量。
     * 输出：返回文章卡片所需的详情数据列表。
     */
    List<ArticleDetailDO> findFeedArticles(Long authorId, int offset, int limit);

    /**
     * 作用：统计首页 Feed 的文章总数。
     * 输入：authorId 为可选作者 ID。
     * 输出：返回满足条件的文章数量。
     */
    long countFeedArticles(Long authorId);

    /**
     * 作用：查询用户点赞过的文章列表。
     * 输入：userId 为用户 ID，offset 为偏移量，limit 为每页数量。
     * 输出：返回文章卡片所需的详情数据列表。
     */
    List<ArticleDetailDO> findLikedArticles(long userId, int offset, int limit);

    /**
     * 作用：统计用户点赞过的文章数量。
     * 输入：userId 为用户 ID。
     * 输出：返回生效点赞关系对应的文章数量。
     */
    long countLikedArticles(long userId);

    /**
     * 作用：查询用户收藏的文章列表。
     * 输入：userId 为用户 ID，offset 为偏移量，limit 为每页数量。
     * 输出：返回文章卡片所需的详情数据列表。
     */
    List<ArticleDetailDO> findFavoritedArticles(long userId, int offset, int limit);

    /**
     * 作用：统计用户收藏的文章数量。
     * 输入：userId 为用户 ID。
     * 输出：返回生效收藏关系对应的文章数量。
     */
    long countFavoritedArticles(long userId);

    /**
     * 作用：查询文章图片列表。
     * 输入：articleId 为文章 ID。
     * 输出：返回文章图片列表。
     */
    List<ArticleMediaDO> findMediaByArticleId(long articleId);

    /**
     * 作用：更新文章主表数据。
     * 输入：article 为更新后的文章对象。
     * 输出：true 表示更新成功，false 表示未更新。
     */
    boolean updateArticle(ArticleDO article);

    /**
     * 作用：软删除文章。
     * 输入：articleId 为文章 ID，authorId 为作者 ID。
     * 输出：true 表示删除成功，false 表示文章不存在、作者不匹配或已经删除。
     */
    boolean softDeleteArticle(long articleId, long authorId);

    /**
     * 作用：替换文章图片列表。
     * 输入：articleId 为文章 ID，mediaList 为新的图片列表。
     * 输出：无，先删除旧图片再写入新图片。
     */
    void replaceMediaByArticleId(long articleId, List<ArticleMediaDO> mediaList);
}
