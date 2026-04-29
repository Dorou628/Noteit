package com.example.noteit.common.event;

import com.example.noteit.article.domain.FeedFanoutGateway;
import com.example.noteit.article.model.ArticleDO;
import com.example.noteit.article.repository.ArticleRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedEventOutboxHandlerTest {

    /**
     * 作用：验证新格式文章发布事件携带 authorId 后，feed handler 不再为了作者 ID 查询文章表。
     * 输入：payload 包含 authorId 的 ArticlePublished outbox 事件。
     * 输出：断言直接调用 feedFanoutGateway，且 ArticleRepository 未被调用。
     */
    @Test
    void articlePublishedShouldUseAuthorIdFromPayloadWithoutQueryingArticle() {
        FeedFanoutGateway feedFanoutGateway = mock(FeedFanoutGateway.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        FeedEventOutboxHandler handler = new FeedEventOutboxHandler(feedFanoutGateway, articleRepository);

        handler.handle(event("ArticlePublished", "1001", "{\"authorId\":101}"));

        verify(feedFanoutGateway).onArticlePublished(101L, 1001L);
        verify(articleRepository, never()).findArticleById(1001L);
    }

    /**
     * 作用：验证老格式文章事件缺少 authorId 时仍可兜底查库，避免历史 outbox 消息无法消费。
     * 输入：payload 不含 authorId 的 ArticleDeleted outbox 事件。
     * 输出：断言通过 ArticleRepository 读取作者 ID 后执行删除分发。
     */
    @Test
    void articleDeletedShouldFallbackToArticleRepositoryForOldPayload() {
        FeedFanoutGateway feedFanoutGateway = mock(FeedFanoutGateway.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        FeedEventOutboxHandler handler = new FeedEventOutboxHandler(feedFanoutGateway, articleRepository);
        when(articleRepository.findArticleById(1001L)).thenReturn(Optional.of(article(1001L, 101L)));

        handler.handle(event("ArticleDeleted", "1001", "{}"));

        verify(feedFanoutGateway).onArticleDeleted(101L, 1001L);
        verify(articleRepository).findArticleById(1001L);
    }

    private static EventOutboxDO event(String eventType, String aggregateId, String payload) {
        return new EventOutboxDO(
                1L,
                "evt-1",
                eventType,
                "ARTICLE",
                aggregateId,
                payload,
                EventOutboxStatus.NEW,
                0,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 4, 27, 18, 0),
                LocalDateTime.of(2026, 4, 27, 18, 0)
        );
    }

    private static ArticleDO article(long articleId, long authorId) {
        return new ArticleDO(
                articleId,
                authorId,
                "title",
                "content",
                "DB",
                null,
                null,
                "TEXT",
                "preview",
                null,
                null,
                1,
                0,
                0,
                0,
                null,
                0,
                0,
                null,
                LocalDateTime.of(2026, 4, 27, 18, 0),
                0,
                LocalDateTime.of(2026, 4, 27, 18, 0),
                LocalDateTime.of(2026, 4, 27, 18, 0)
        );
    }
}
