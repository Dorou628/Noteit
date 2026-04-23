package com.example.noteit.interaction.service;

import com.example.noteit.article.model.ArticleCardResponse;
import com.example.noteit.article.model.ArticleDetailDO;
import com.example.noteit.article.model.AuthorView;
import com.example.noteit.article.model.SummaryView;
import com.example.noteit.article.repository.ArticleRepository;
import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.constant.SummaryStatus;
import com.example.noteit.common.event.DomainEventPublisher;
import com.example.noteit.common.exception.BusinessException;
import com.example.noteit.common.id.IdGenerator;
import com.example.noteit.common.response.PageResponse;
import com.example.noteit.interaction.model.ArticleFavoriteDO;
import com.example.noteit.interaction.model.ArticleFavoriteResponse;
import com.example.noteit.interaction.model.ArticleInteractionSnapshot;
import com.example.noteit.interaction.model.ArticleLikeDO;
import com.example.noteit.interaction.model.ArticleLikeResponse;
import com.example.noteit.interaction.repository.InteractionRepository;
import com.example.noteit.relation.repository.RelationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class InteractionApplicationService {

    private static final int ACTIVE_STATUS = 1;
    private static final int INACTIVE_STATUS = 0;

    private final DomainEventPublisher domainEventPublisher;
    private final IdGenerator idGenerator;
    private final ArticleRepository articleRepository;
    private final InteractionRepository interactionRepository;
    private final RelationRepository relationRepository;

    public InteractionApplicationService(
            DomainEventPublisher domainEventPublisher,
            IdGenerator idGenerator,
            ArticleRepository articleRepository,
            InteractionRepository interactionRepository,
            RelationRepository relationRepository
    ) {
        this.domainEventPublisher = domainEventPublisher;
        this.idGenerator = idGenerator;
        this.articleRepository = articleRepository;
        this.interactionRepository = interactionRepository;
        this.relationRepository = relationRepository;
    }

    /**
     * 作用：对文章执行点赞操作，并返回当前点赞状态和最新计数。
     * 输入：userId 为当前用户 ID，articleId 为文章 ID 字符串。
     * 输出：返回点赞结果，包含文章 ID、是否已点赞、最新点赞数。
     */
    @Transactional
    public ArticleLikeResponse likeArticle(Long userId, String articleId) {
        long articleIdValue = parseExistingArticleId(articleId);
        ArticleLikeDO existing = interactionRepository.findLikeRelation(articleIdValue, userId).orElse(null);
        if (existing == null) {
            interactionRepository.insertLikeRelation(new ArticleLikeDO(
                    idGenerator.nextId(),
                    articleIdValue,
                    userId,
                    ACTIVE_STATUS,
                    null,
                    null
            ));
        } else if (existing.status() != ACTIVE_STATUS) {
            interactionRepository.updateLikeStatus(existing.id(), ACTIVE_STATUS);
        }

        ArticleInteractionSnapshot snapshot = getInteractionSnapshot(articleIdValue, userId);
        return new ArticleLikeResponse(articleId, snapshot.liked(), snapshot.likeCount());
    }

    /**
     * 作用：对文章执行取消点赞操作，并返回当前点赞状态和最新计数。
     * 输入：userId 为当前用户 ID，articleId 为文章 ID 字符串。
     * 输出：返回点赞结果，包含文章 ID、是否已点赞、最新点赞数。
     */
    @Transactional
    public ArticleLikeResponse unlikeArticle(Long userId, String articleId) {
        long articleIdValue = parseExistingArticleId(articleId);
        interactionRepository.findLikeRelation(articleIdValue, userId)
                .filter(relation -> relation.status() != INACTIVE_STATUS)
                .ifPresent(relation -> interactionRepository.updateLikeStatus(relation.id(), INACTIVE_STATUS));

        ArticleInteractionSnapshot snapshot = getInteractionSnapshot(articleIdValue, userId);
        return new ArticleLikeResponse(articleId, snapshot.liked(), snapshot.likeCount());
    }

    /**
     * 作用：对文章执行收藏操作，并返回当前收藏状态和最新计数。
     * 输入：userId 为当前用户 ID，articleId 为文章 ID 字符串。
     * 输出：返回收藏结果，包含文章 ID、是否已收藏、最新收藏数。
     */
    @Transactional
    public ArticleFavoriteResponse favoriteArticle(Long userId, String articleId) {
        long articleIdValue = parseExistingArticleId(articleId);
        ArticleFavoriteDO existing = interactionRepository.findFavoriteRelation(articleIdValue, userId).orElse(null);
        if (existing == null) {
            interactionRepository.insertFavoriteRelation(new ArticleFavoriteDO(
                    idGenerator.nextId(),
                    articleIdValue,
                    userId,
                    ACTIVE_STATUS,
                    null,
                    null
            ));
        } else if (existing.status() != ACTIVE_STATUS) {
            interactionRepository.updateFavoriteStatus(existing.id(), ACTIVE_STATUS);
        }

        ArticleInteractionSnapshot snapshot = getInteractionSnapshot(articleIdValue, userId);
        return new ArticleFavoriteResponse(articleId, snapshot.favorited(), snapshot.favoriteCount());
    }

    /**
     * 作用：对文章执行取消收藏操作，并返回当前收藏状态和最新计数。
     * 输入：userId 为当前用户 ID，articleId 为文章 ID 字符串。
     * 输出：返回收藏结果，包含文章 ID、是否已收藏、最新收藏数。
     */
    @Transactional
    public ArticleFavoriteResponse unfavoriteArticle(Long userId, String articleId) {
        long articleIdValue = parseExistingArticleId(articleId);
        interactionRepository.findFavoriteRelation(articleIdValue, userId)
                .filter(relation -> relation.status() != INACTIVE_STATUS)
                .ifPresent(relation -> interactionRepository.updateFavoriteStatus(relation.id(), INACTIVE_STATUS));

        ArticleInteractionSnapshot snapshot = getInteractionSnapshot(articleIdValue, userId);
        return new ArticleFavoriteResponse(articleId, snapshot.favorited(), snapshot.favoriteCount());
    }

    /**
     * 作用：查询文章的互动快照，供文章详情页返回真实互动状态。
     * 输入：articleId 为文章 ID，currentUserId 为当前用户 ID，可为空。
     * 输出：返回点赞数、收藏数、当前用户点赞状态、当前用户收藏状态。
     */
    public ArticleInteractionSnapshot getInteractionSnapshot(long articleId, Long currentUserId) {
        long likeCount = interactionRepository.countActiveLikes(articleId);
        long favoriteCount = interactionRepository.countActiveFavorites(articleId);
        boolean liked = currentUserId != null && interactionRepository.hasActiveLike(articleId, currentUserId);
        boolean favorited = currentUserId != null && interactionRepository.hasActiveFavorite(articleId, currentUserId);
        return new ArticleInteractionSnapshot(likeCount, favoriteCount, liked, favorited);
    }

    /**
     * 作用：查询“我点赞过的文章”列表。
     * 输入：userId 为当前用户 ID，pageNo 和 pageSize 为分页参数。
     * 输出：返回用户生效点赞关系对应的文章卡片分页结果。
     */
    public PageResponse<ArticleCardResponse> getLikedArticles(Long userId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<ArticleDetailDO> articles = articleRepository.findLikedArticles(userId, offset, pageSize);
        long total = articleRepository.countLikedArticles(userId);
        List<ArticleCardResponse> records = articles.stream()
                .map(article -> toCardResponse(article, userId))
                .toList();
        return new PageResponse<>(pageNo, pageSize, total, records);
    }

    /**
     * 作用：查询“我收藏的文章”列表。
     * 输入：userId 为当前用户 ID，pageNo 和 pageSize 为分页参数。
     * 输出：返回用户生效收藏关系对应的文章卡片分页结果。
     */
    public PageResponse<ArticleCardResponse> getFavoritedArticles(Long userId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<ArticleDetailDO> articles = articleRepository.findFavoritedArticles(userId, offset, pageSize);
        long total = articleRepository.countFavoritedArticles(userId);
        List<ArticleCardResponse> records = articles.stream()
                .map(article -> toCardResponse(article, userId))
                .toList();
        return new PageResponse<>(pageNo, pageSize, total, records);
    }

    /**
     * 作用：把数据库文章数据组装成当前用户视角下的文章卡片。
     * 输入：detail 为文章详情数据，currentUserId 为当前用户 ID。
     * 输出：返回带互动状态的文章卡片。
     */
    private ArticleCardResponse toCardResponse(ArticleDetailDO detail, Long currentUserId) {
        OffsetDateTime updatedAt = toOffsetDateTime(detail.updatedAt());
        ArticleInteractionSnapshot interactionSnapshot = getInteractionSnapshot(detail.id(), currentUserId);
        return new ArticleCardResponse(
                String.valueOf(detail.id()),
                detail.title(),
                detail.contentPreview(),
                detail.coverUrl(),
                new AuthorView(
                        String.valueOf(detail.authorId()),
                        StringUtils.hasText(detail.authorNickname()) ? detail.authorNickname() : "User-" + detail.authorId(),
                        detail.authorAvatarUrl(),
                        isAuthorFollowed(currentUserId, detail.authorId())
                ),
                new SummaryView(
                        SummaryStatus.fromCode(detail.summaryStatus()),
                        detail.summaryText(),
                        updatedAt
                ),
                interactionSnapshot.likeCount(),
                interactionSnapshot.favoriteCount(),
                interactionSnapshot.liked(),
                interactionSnapshot.favorited(),
                toOffsetDateTime(detail.createdAt()),
                updatedAt
        );
    }

    private boolean isAuthorFollowed(Long currentUserId, long authorId) {
        return currentUserId != null
                && currentUserId != authorId
                && relationRepository.hasActiveFollow(currentUserId, authorId);
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        return value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    /**
     * 作用：解析文章 ID 并确认文章存在，避免互动关系写入脏数据。
     * 输入：articleId 为外部传入的文章 ID 字符串。
     * 输出：返回合法的 long 类型文章 ID。
     */
    private long parseExistingArticleId(String articleId) {
        try {
            long articleIdValue = Long.parseLong(articleId);
            if (articleRepository.findArticleById(articleIdValue).isEmpty()) {
                throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
            }
            return articleIdValue;
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
    }
}
