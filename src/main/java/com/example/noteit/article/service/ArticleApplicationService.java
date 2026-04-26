package com.example.noteit.article.service;

import com.example.noteit.article.domain.ArticleContentStorageGateway;
import com.example.noteit.article.domain.CachedTimelinePage;
import com.example.noteit.article.domain.FeedTimelineCache;
import com.example.noteit.article.domain.StoredArticleContent;
import com.example.noteit.article.event.ArticleDeletedEvent;
import com.example.noteit.article.event.ArticlePublishedEvent;
import com.example.noteit.article.event.ArticleUpdatedEvent;
import com.example.noteit.article.model.ArticleCardResponse;
import com.example.noteit.article.model.ArticleDO;
import com.example.noteit.article.model.ArticleDetailDO;
import com.example.noteit.article.model.ArticleDetailResponse;
import com.example.noteit.article.model.ArticleFeedQuery;
import com.example.noteit.article.model.ArticleImageRequest;
import com.example.noteit.article.model.ArticleMediaDO;
import com.example.noteit.article.model.AuthorView;
import com.example.noteit.article.model.CreateArticleRequest;
import com.example.noteit.article.model.FeedTimelineEntryDO;
import com.example.noteit.article.model.SummaryView;
import com.example.noteit.article.model.UpdateArticleRequest;
import com.example.noteit.article.repository.ArticleRepository;
import com.example.noteit.article.repository.FeedTimelineRepository;
import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.constant.SummaryStatus;
import com.example.noteit.common.context.UserContext;
import com.example.noteit.common.context.UserContextHolder;
import com.example.noteit.common.event.DomainEventPublisher;
import com.example.noteit.common.exception.BusinessException;
import com.example.noteit.common.id.IdGenerator;
import com.example.noteit.common.response.PageResponse;
import com.example.noteit.common.util.TimeProvider;
import com.example.noteit.interaction.model.ArticleInteractionSnapshot;
import com.example.noteit.interaction.service.InteractionApplicationService;
import com.example.noteit.relation.repository.RelationRepository;
import com.example.noteit.user.model.UserProfileDO;
import com.example.noteit.user.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ArticleApplicationService {

    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;
    private final DomainEventPublisher domainEventPublisher;
    private final ArticleRepository articleRepository;
    private final UserProfileRepository userProfileRepository;
    private final ArticleContentStorageGateway articleContentStorageGateway;
    private final FeedTimelineRepository feedTimelineRepository;
    private final FeedTimelineCache feedTimelineCache;
    private final InteractionApplicationService interactionApplicationService;
    private final RelationRepository relationRepository;
    private final int feedCacheRebuildLimit;

    public ArticleApplicationService(
            IdGenerator idGenerator,
            TimeProvider timeProvider,
            DomainEventPublisher domainEventPublisher,
            ArticleRepository articleRepository,
            UserProfileRepository userProfileRepository,
            ArticleContentStorageGateway articleContentStorageGateway,
            FeedTimelineRepository feedTimelineRepository,
            FeedTimelineCache feedTimelineCache,
            InteractionApplicationService interactionApplicationService,
            RelationRepository relationRepository,
            @Value("${noteit.feed.cache.rebuild-limit:1000}") int feedCacheRebuildLimit
    ) {
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
        this.domainEventPublisher = domainEventPublisher;
        this.articleRepository = articleRepository;
        this.userProfileRepository = userProfileRepository;
        this.articleContentStorageGateway = articleContentStorageGateway;
        this.feedTimelineRepository = feedTimelineRepository;
        this.feedTimelineCache = feedTimelineCache;
        this.interactionApplicationService = interactionApplicationService;
        this.relationRepository = relationRepository;
        this.feedCacheRebuildLimit = feedCacheRebuildLimit;
    }

    /**
     * 作用：获取首页 Feed 的分页数据。
     * 输入：query 为分页和作者筛选参数，currentUserId 为当前登录用户 ID，可为空。
     * 输出：返回文章卡片分页结果，卡片包含作者、摘要、互动计数和当前用户互动状态。
     */
    public PageResponse<ArticleCardResponse> getFeed(ArticleFeedQuery query, Long currentUserId) {
        int pageNo = query.normalizedPageNo();
        int pageSize = query.normalizedPageSize();
        Long authorId = parseOptionalAuthorId(query.authorId());
        return getPublishedArticles(authorId, currentUserId, pageNo, pageSize);
    }

    /**
     * 作用：查询指定作者已发布文章列表。
     * 输入：authorId 为作者 ID，currentUserId 为当前登录用户 ID，可为空，pageNo/pageSize 为分页参数。
     * 输出：返回文章卡片分页结果。
     */
    public PageResponse<ArticleCardResponse> getPublishedArticles(
            Long authorId,
            Long currentUserId,
            int pageNo,
            int pageSize
    ) {
        int offset = (pageNo - 1) * pageSize;
        List<ArticleDetailDO> articles = articleRepository.findFeedArticles(authorId, offset, pageSize);
        long total = articleRepository.countFeedArticles(authorId);
        List<ArticleCardResponse> records = articles.stream()
                .map(article -> toCardResponse(article, currentUserId))
                .toList();
        return new PageResponse<>(pageNo, pageSize, total, records);
    }

    public PageResponse<ArticleCardResponse> getFollowingFeed(Long currentUserId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        Optional<CachedTimelinePage> cachedPage = feedTimelineCache.findInboxPage(currentUserId, offset, pageSize);
        if (cachedPage.isPresent()) {
            CachedTimelinePage page = cachedPage.get();
            List<ArticleDetailDO> articles = findDetailsInTimelineOrder(page.articleIds());
            List<ArticleCardResponse> records = articles.stream()
                    .map(article -> toCardResponse(article, currentUserId))
                    .toList();
            return new PageResponse<>(pageNo, pageSize, page.total(), records);
        }

        List<ArticleDetailDO> articles = feedTimelineRepository.findInboxArticles(currentUserId, offset, pageSize);
        long total = feedTimelineRepository.countInboxArticles(currentUserId);
        rebuildInboxCache(currentUserId, total);
        List<ArticleCardResponse> records = articles.stream()
                .map(article -> toCardResponse(article, currentUserId))
                .toList();
        return new PageResponse<>(pageNo, pageSize, total, records);
    }

    private void rebuildInboxCache(Long currentUserId, long total) {
        List<FeedTimelineEntryDO> entries = feedTimelineRepository.findRecentInboxEntries(
                currentUserId,
                Math.max(feedCacheRebuildLimit, 0)
        );
        feedTimelineCache.rebuildInbox(currentUserId, entries, total);
    }

    private List<ArticleDetailDO> findDetailsInTimelineOrder(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return List.of();
        }
        Map<Long, ArticleDetailDO> detailById = new HashMap<>();
        for (ArticleDetailDO detail : articleRepository.findDetailsByIds(articleIds)) {
            detailById.put(detail.id(), detail);
        }
        List<ArticleDetailDO> ordered = new ArrayList<>();
        for (Long articleId : articleIds) {
            ArticleDetailDO detail = detailById.get(articleId);
            if (detail != null) {
                ordered.add(detail);
            }
        }
        return ordered;
    }

    /**
     * 作用：获取文章详情，并补充真实互动计数和当前用户互动状态。
     * 输入：articleId 为文章 ID 字符串；currentUserId 为当前用户 ID，可为空。
     * 输出：返回完整文章详情。
     */
    public ArticleDetailResponse getArticleDetail(String articleId, Long currentUserId) {
        long articleIdValue = parseArticleId(articleId);
        ArticleDetailDO detail = articleRepository.findDetailById(articleIdValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));
        List<ArticleMediaDO> mediaList = articleRepository.findMediaByArticleId(articleIdValue);
        ArticleInteractionSnapshot interactionSnapshot =
                interactionApplicationService.getInteractionSnapshot(articleIdValue, currentUserId);
        return toDetailResponse(detail, mediaList, interactionSnapshot, currentUserId);
    }

    /**
     * 作用：创建新文章并返回详情。
     * 输入：authorId 为作者用户 ID，request 为文章创建请求。
     * 输出：返回持久化后的文章详情。
     */
    @Transactional
    public ArticleDetailResponse createArticle(Long authorId, CreateArticleRequest request) {
        validateArticleRequest(request.title(), request.content());

        long articleId = idGenerator.nextId();
        OffsetDateTime now = timeProvider.now();
        ensureAuthorProfileExists(authorId);
        StoredArticleContent storedContent = articleContentStorageGateway.store(
                request.content(),
                request.contentFormat(),
                request.contentPreview()
        );

        articleRepository.insertArticle(new ArticleDO(
                articleId,
                authorId,
                request.title(),
                storedContent.contentText(),
                storedContent.contentStorageType(),
                storedContent.contentObjectKey(),
                storedContent.contentUrl(),
                storedContent.contentFormat(),
                storedContent.contentPreview(),
                request.coverObjectKey(),
                request.coverUrl(),
                1,
                0,
                0,
                SummaryStatus.PENDING.code(),
                null,
                0,
                0,
                null,
                now.toLocalDateTime(),
                0,
                null,
                null
        ));
        articleRepository.insertMedia(buildMediaList(articleId, request.images()));
        userProfileRepository.incrementArticleCount(authorId, 1);

        domainEventPublisher.publish(new ArticlePublishedEvent(String.valueOf(articleId), now));
        return getArticleDetail(String.valueOf(articleId), authorId);
    }

    /**
     * 作用：更新文章内容，并在成功后返回最新详情。
     * 输入：authorId 为当前用户 ID，articleId 为文章 ID 字符串，request 为更新请求。
     * 输出：返回更新后的文章详情。
     */
    @Transactional
    public ArticleDetailResponse updateArticle(Long authorId, String articleId, UpdateArticleRequest request) {
        validateArticleRequest(request.title(), request.content());

        long articleIdValue = parseArticleId(articleId);
        ArticleDO existing = articleRepository.findArticleById(articleIdValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));
        if (existing.status() != 1) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        if (existing.authorId() != authorId) {
            throw new BusinessException(ErrorCode.ARTICLE_AUTHOR_MISMATCH);
        }

        OffsetDateTime now = timeProvider.now();
        StoredArticleContent storedContent = articleContentStorageGateway.store(
                request.content(),
                request.contentFormat(),
                request.contentPreview()
        );
        articleRepository.updateArticle(new ArticleDO(
                existing.id(),
                existing.authorId(),
                request.title(),
                storedContent.contentText(),
                storedContent.contentStorageType(),
                storedContent.contentObjectKey(),
                storedContent.contentUrl(),
                storedContent.contentFormat(),
                storedContent.contentPreview(),
                request.coverObjectKey(),
                request.coverUrl(),
                existing.status(),
                existing.likeCount(),
                existing.favoriteCount(),
                SummaryStatus.PENDING.code(),
                null,
                existing.summaryVersion() + 1,
                0,
                null,
                existing.publishedAt(),
                existing.version() + 1,
                existing.createdAt(),
                now.toLocalDateTime()
        ));
        articleRepository.replaceMediaByArticleId(articleIdValue, buildMediaList(articleIdValue, request.images()));

        domainEventPublisher.publish(new ArticleUpdatedEvent(articleId, now));
        return getArticleDetail(articleId, authorId);
    }

    /**
     * 作用：删除当前用户自己的文章，并清理 Feed 时间线与缓存。
     * 输入：authorId 为当前用户 ID，articleId 为文章 ID 字符串。
     * 输出：无；若文章不存在、已删除或非本人文章则抛业务异常。
     */
    @Transactional
    public void deleteArticle(Long authorId, String articleId) {
        long articleIdValue = parseArticleId(articleId);
        ArticleDO existing = articleRepository.findArticleById(articleIdValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));
        if (existing.status() != 1) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        if (existing.authorId() != authorId) {
            throw new BusinessException(ErrorCode.ARTICLE_AUTHOR_MISMATCH);
        }

        boolean deleted = articleRepository.softDeleteArticle(articleIdValue, authorId);
        if (!deleted) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        userProfileRepository.incrementArticleCount(authorId, -1);
        domainEventPublisher.publish(new ArticleDeletedEvent(articleId, timeProvider.now()));
    }

    /**
     * 作用：校验文章标题和正文是否满足最小入库要求。
     * 输入：title 为文章标题，content 为正文内容。
     * 输出：无；若参数非法则抛业务异常。
     */
    private void validateArticleRequest(String title, String content) {
        if (!StringUtils.hasText(title)) {
            throw new BusinessException(ErrorCode.ARTICLE_TITLE_EMPTY);
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ErrorCode.ARTICLE_CONTENT_EMPTY);
        }
    }

    /**
     * 作用：确保作者在 user_profile 中有基础资料，避免文章详情缺少作者信息。
     * 输入：authorId 为作者用户 ID。
     * 输出：无；若资料不存在则自动补一条最小可用记录。
     */
    private void ensureAuthorProfileExists(Long authorId) {
        Optional<UserProfileDO> existing = userProfileRepository.findById(authorId);
        if (existing.isPresent()) {
            return;
        }

        String nickname = UserContextHolder.get()
                .map(UserContext::nickname)
                .filter(StringUtils::hasText)
                .orElse("User-" + authorId);

        userProfileRepository.insert(new UserProfileDO(
                authorId,
                nickname,
                null,
                null,
                0,
                0,
                0,
                1,
                0,
                null,
                null
        ));
    }

    /**
     * 作用：把请求中的图片列表转换成 article_media 持久化对象。
     * 输入：articleId 为文章 ID，images 为前端提交的图片列表。
     * 输出：返回可直接落库的媒体列表；没有图片时返回空列表。
     */
    private List<ArticleMediaDO> buildMediaList(long articleId, List<ArticleImageRequest> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        List<ArticleMediaDO> mediaList = new ArrayList<>(images.size());
        for (ArticleImageRequest image : images) {
            mediaList.add(new ArticleMediaDO(
                    idGenerator.nextId(),
                    articleId,
                    "IMAGE",
                    image.objectKey(),
                    image.url(),
                    image.sortNo(),
                    image.isCover(),
                    null,
                    null
            ));
        }
        return mediaList;
    }

    /**
     * 作用：把数据库文章数据组装成 Feed 卡片响应。
     * 输入：detail 为文章详情数据，currentUserId 为当前用户 ID，可为空。
     * 输出：返回可直接展示在首页 Feed 的文章卡片。
     */
    private ArticleCardResponse toCardResponse(ArticleDetailDO detail, Long currentUserId) {
        OffsetDateTime updatedAt = toOffsetDateTime(detail.updatedAt());
        ArticleInteractionSnapshot interactionSnapshot =
                interactionApplicationService.getInteractionSnapshot(detail.id(), currentUserId);
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

    /**
     * 作用：把数据库详情对象、媒体列表和互动快照组装成接口响应。
     * 输入：detail 为文章详情数据，mediaList 为文章图片列表，interactionSnapshot 为互动快照。
     * 输出：返回可直接给前端的 ArticleDetailResponse。
     */
    private ArticleDetailResponse toDetailResponse(
            ArticleDetailDO detail,
            List<ArticleMediaDO> mediaList,
            ArticleInteractionSnapshot interactionSnapshot,
            Long currentUserId
    ) {
        OffsetDateTime updatedAt = toOffsetDateTime(detail.updatedAt());
        return new ArticleDetailResponse(
                String.valueOf(detail.id()),
                detail.title(),
                articleContentStorageGateway.load(
                        detail.contentStorageType(),
                        detail.contentText(),
                        detail.contentObjectKey(),
                        detail.contentUrl()
                ),
                detail.contentObjectKey(),
                detail.contentUrl(),
                detail.contentFormat(),
                detail.contentPreview(),
                detail.coverUrl(),
                mediaList.stream().map(ArticleMediaDO::mediaUrl).toList(),
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

    /**
     * 作用：解析可选作者 ID 参数。
     * 输入：rawAuthorId 为 Feed 查询里的作者 ID 字符串，可为空。
     * 输出：返回 Long 类型作者 ID；为空时返回 null，格式非法时抛参数异常。
     */
    private Long parseOptionalAuthorId(String rawAuthorId) {
        if (!StringUtils.hasText(rawAuthorId)) {
            return null;
        }
        try {
            return Long.parseLong(rawAuthorId);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "authorId must be a number");
        }
    }

    /**
     * 作用：把外部文章 ID 转成 long。
     * 输入：rawId 为路径或请求中的文章 ID 字符串。
     * 输出：返回 long 类型文章 ID；若格式非法则抛文章不存在异常。
     */
    private long parseArticleId(String rawId) {
        try {
            return Long.parseLong(rawId);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
    }

    /**
     * 作用：把本地时间转换成带时区的接口响应时间。
     * 输入：value 为数据库中的 LocalDateTime。
     * 输出：返回 OffsetDateTime，供响应直接使用。
     */
    private OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        return value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
