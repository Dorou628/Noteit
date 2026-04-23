package com.example.noteit.article.repository;

import com.example.noteit.article.model.ArticleDO;
import com.example.noteit.article.model.ArticleDetailDO;
import com.example.noteit.article.model.ArticleMediaDO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisArticleRepository implements ArticleRepository {

    private final ArticleMapper articleMapper;
    private final ArticleMediaMapper articleMediaMapper;

    public MyBatisArticleRepository(ArticleMapper articleMapper, ArticleMediaMapper articleMediaMapper) {
        this.articleMapper = articleMapper;
        this.articleMediaMapper = articleMediaMapper;
    }

    /**
     * 作用：保存文章主表数据。
     * 输入：article 为待保存的文章对象。
     * 输出：无，委托 MyBatis Mapper 写入数据库。
     */
    @Override
    public void insertArticle(ArticleDO article) {
        articleMapper.insert(article);
    }

    /**
     * 作用：批量保存文章图片数据。
     * 输入：mediaList 为文章图片列表。
     * 输出：无；空列表时直接返回。
     */
    @Override
    public void insertMedia(List<ArticleMediaDO> mediaList) {
        if (mediaList == null || mediaList.isEmpty()) {
            return;
        }
        articleMediaMapper.batchInsert(mediaList);
    }

    /**
     * 作用：按文章 ID 查询文章主表数据。
     * 输入：articleId 为文章 ID。
     * 输出：返回文章对象，找不到则返回空。
     */
    @Override
    public Optional<ArticleDO> findArticleById(long articleId) {
        return Optional.ofNullable(articleMapper.findById(articleId));
    }

    /**
     * 作用：按文章 ID 查询文章详情数据。
     * 输入：articleId 为文章 ID。
     * 输出：返回文章详情对象，找不到则返回空。
     */
    @Override
    public Optional<ArticleDetailDO> findDetailById(long articleId) {
        return Optional.ofNullable(articleMapper.findDetailById(articleId));
    }

    /**
     * 作用：分页查询首页 Feed 文章。
     * 输入：authorId 为可选作者 ID，offset 为偏移量，limit 为每页数量。
     * 输出：返回文章详情列表，供服务层组装卡片。
     */
    @Override
    public List<ArticleDetailDO> findFeedArticles(Long authorId, int offset, int limit) {
        return articleMapper.findFeedArticles(authorId, offset, limit);
    }

    /**
     * 作用：统计首页 Feed 文章总数。
     * 输入：authorId 为可选作者 ID。
     * 输出：返回满足条件的文章数量。
     */
    @Override
    public long countFeedArticles(Long authorId) {
        return articleMapper.countFeedArticles(authorId);
    }

    /**
     * 作用：分页查询用户点赞过的文章。
     * 输入：userId 为用户 ID，offset 为偏移量，limit 为每页数量。
     * 输出：返回文章详情列表，供服务层组装卡片。
     */
    @Override
    public List<ArticleDetailDO> findLikedArticles(long userId, int offset, int limit) {
        return articleMapper.findLikedArticles(userId, offset, limit);
    }

    /**
     * 作用：统计用户点赞过的文章数量。
     * 输入：userId 为用户 ID。
     * 输出：返回满足条件的文章数量。
     */
    @Override
    public long countLikedArticles(long userId) {
        return articleMapper.countLikedArticles(userId);
    }

    /**
     * 作用：分页查询用户收藏的文章。
     * 输入：userId 为用户 ID，offset 为偏移量，limit 为每页数量。
     * 输出：返回文章详情列表，供服务层组装卡片。
     */
    @Override
    public List<ArticleDetailDO> findFavoritedArticles(long userId, int offset, int limit) {
        return articleMapper.findFavoritedArticles(userId, offset, limit);
    }

    /**
     * 作用：统计用户收藏的文章数量。
     * 输入：userId 为用户 ID。
     * 输出：返回满足条件的文章数量。
     */
    @Override
    public long countFavoritedArticles(long userId) {
        return articleMapper.countFavoritedArticles(userId);
    }

    /**
     * 作用：查询文章图片列表。
     * 输入：articleId 为文章 ID。
     * 输出：返回文章图片列表。
     */
    @Override
    public List<ArticleMediaDO> findMediaByArticleId(long articleId) {
        return articleMediaMapper.findByArticleId(articleId);
    }

    /**
     * 作用：更新文章主表数据。
     * 输入：article 为更新后的文章对象。
     * 输出：true 表示至少更新一行。
     */
    @Override
    public boolean updateArticle(ArticleDO article) {
        return articleMapper.update(article) > 0;
    }

    /**
     * 作用：替换文章图片列表。
     * 输入：articleId 为文章 ID，mediaList 为新的图片列表。
     * 输出：无，先删旧数据再插入新数据。
     */
    @Override
    public void replaceMediaByArticleId(long articleId, List<ArticleMediaDO> mediaList) {
        articleMediaMapper.deleteByArticleId(articleId);
        insertMedia(mediaList);
    }
}
