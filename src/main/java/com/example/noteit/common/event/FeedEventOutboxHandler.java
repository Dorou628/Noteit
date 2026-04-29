package com.example.noteit.common.event;

import com.example.noteit.article.domain.FeedFanoutGateway;
import com.example.noteit.article.model.ArticleDO;
import com.example.noteit.article.repository.ArticleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * 作用：消费文章和关注关系相关的 outbox 事件，并维护 feed inbox/outbox 缓存与分发表。
 */
@Component
public class FeedEventOutboxHandler implements EventOutboxHandler {

    private final FeedFanoutGateway feedFanoutGateway;
    private final ArticleRepository articleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 作用：注入 feed 分发网关和文章仓储。
     * 输入：feedFanoutGateway 用于执行推拉结合分发，articleRepository 用于读取文章状态和作者。
     * 输出：构造完成后的 feed 事件处理器实例。
     */
    public FeedEventOutboxHandler(
            FeedFanoutGateway feedFanoutGateway,
            ArticleRepository articleRepository
    ) {
        this.feedFanoutGateway = feedFanoutGateway;
        this.articleRepository = articleRepository;
    }

    @Override
    /**
     * 作用：判断当前处理器是否支持指定事件类型。
     * 输入：eventType 为 outbox 事件类型。
     * 输出：文章发布、删除、更新以及关注关系变化返回 true，其它返回 false。
     */
    public boolean supports(String eventType) {
        return "ArticlePublished".equals(eventType)
                || "ArticleDeleted".equals(eventType)
                || "FollowRelationshipChanged".equals(eventType)
                || "ArticleUpdated".equals(eventType);
    }

    @Override
    /**
     * 作用：根据事件类型路由到具体 feed 处理逻辑。
     * 输入：event 为待消费的 outbox 事件。
     * 输出：无返回值；payload 解析或分发失败时抛出异常交由上游重试。
     */
    public void handle(EventOutboxDO event) {
        switch (event.eventType()) {
            case "ArticlePublished" -> handleArticlePublished(event);
            case "ArticleDeleted" -> handleArticleDeleted(event);
            case "FollowRelationshipChanged" -> handleFollowRelationshipChanged(event);
            case "ArticleUpdated" -> {
                // feed 时间线只缓存文章 ID，文章详情仍从 MySQL 读取，因此文章更新不需要调整时间线缓存。
            }
            default -> {
            }
        }
    }

    /**
     * 作用：处理文章发布事件，将新文章推送到粉丝 inbox 并更新作者 outbox。
     * 输入：event.aggregateId 为文章 ID。
     * 输出：无返回值；文章不存在或已删除时跳过。
     */
    private void handleArticlePublished(EventOutboxDO event) {
        long articleId = Long.parseLong(event.aggregateId());
        Long authorId = readAuthorId(event);
        if (authorId == null) {
            ArticleDO article = articleRepository.findArticleById(articleId).orElse(null);
            if (article == null || article.status() != 1) {
                return;
            }
            authorId = article.authorId();
        }
        if (authorId == null) {
            return;
        }
        feedFanoutGateway.onArticlePublished(authorId, articleId);
    }

    /**
     * 作用：处理文章删除事件，从粉丝 inbox 和作者 outbox 中移除文章 ID。
     * 输入：event.aggregateId 为文章 ID。
     * 输出：无返回值；文章记录不存在时跳过。
     */
    private void handleArticleDeleted(EventOutboxDO event) {
        long articleId = Long.parseLong(event.aggregateId());
        Long authorId = readAuthorId(event);
        if (authorId == null) {
            ArticleDO article = articleRepository.findArticleById(articleId).orElse(null);
            if (article == null) {
                return;
            }
            authorId = article.authorId();
        }
        if (authorId == null) {
            return;
        }
        feedFanoutGateway.onArticleDeleted(authorId, articleId);
    }

    /**
     * 作用：处理关注或取关事件，关注时拉取博主 outbox，取关时移除该博主文章。
     * 输入：event.payload 包含 followerUserId、followeeUserId 和 following。
     * 输出：无返回值；payload 非法时抛出异常。
     */
    private void handleFollowRelationshipChanged(EventOutboxDO event) {
        JsonNode payload = readPayload(event.payload());
        long followerUserId = payload.path("followerUserId").asLong();
        long followeeUserId = payload.path("followeeUserId").asLong();
        boolean following = payload.path("following").asBoolean();
        feedFanoutGateway.onFollowRelationshipChanged(followerUserId, followeeUserId, following);
    }

    /**
     * 作用：解析 outbox payload JSON。
     * 输入：payload 为数据库保存的事件 JSON 字符串。
     * 输出：JsonNode；解析失败时抛出异常。
     */
    private JsonNode readPayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid event payload", ex);
        }
    }

    /**
     * 作用：从文章事件 payload 中读取作者 ID，新消息应直接携带该字段以避免消费端再次查库。
     * 输入：event 为文章相关 outbox 事件。
     * 输出：payload 中的 authorId；老消息缺少该字段时返回 null。
     */
    private Long readAuthorId(EventOutboxDO event) {
        JsonNode payload = readPayload(event.payload());
        JsonNode authorIdNode = payload.path("authorId");
        return authorIdNode.isMissingNode() || authorIdNode.isNull() ? null : authorIdNode.asLong();
    }
}
