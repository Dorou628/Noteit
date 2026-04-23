package com.example.noteit.article.service;

import com.example.noteit.article.model.ArticleCardResponse;
import com.example.noteit.article.model.ArticleDetailResponse;
import com.example.noteit.article.model.ArticleFeedQuery;
import com.example.noteit.article.model.ArticleImageRequest;
import com.example.noteit.article.model.CreateArticleRequest;
import com.example.noteit.article.model.UpdateArticleRequest;
import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.exception.BusinessException;
import com.example.noteit.common.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ArticleApplicationServiceTest {

    @Autowired
    private ArticleApplicationService articleApplicationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM article_like");
        jdbcTemplate.update("DELETE FROM article_favorite");
        jdbcTemplate.update("DELETE FROM user_follow");
        jdbcTemplate.update("DELETE FROM article_media");
        jdbcTemplate.update("DELETE FROM article");
        jdbcTemplate.update("DELETE FROM user_profile");
    }

    @Test
    void createArticleShouldPersistContentInDatabase() {
        ArticleDetailResponse response = articleApplicationService.createArticle(2001L, createRequest("first"));

        String contentText = jdbcTemplate.queryForObject(
                "SELECT content_text FROM article WHERE id = ?",
                String.class,
                Long.parseLong(response.id())
        );
        String contentStorageType = jdbcTemplate.queryForObject(
                "SELECT content_storage_type FROM article WHERE id = ?",
                String.class,
                Long.parseLong(response.id())
        );
        String contentPreview = jdbcTemplate.queryForObject(
                "SELECT content_preview FROM article WHERE id = ?",
                String.class,
                Long.parseLong(response.id())
        );
        Integer summaryStatus = jdbcTemplate.queryForObject(
                "SELECT summary_status FROM article WHERE id = ?",
                Integer.class,
                Long.parseLong(response.id())
        );

        assertThat(contentText).isEqualTo("# first\nThis is the full body for first.");
        assertThat(contentStorageType).isEqualTo("DB");
        assertThat(contentPreview).isEqualTo("Preview first");
        assertThat(summaryStatus).isEqualTo(0);
        assertThat(response.content()).isEqualTo("# first\nThis is the full body for first.");
        assertThat(response.contentObjectKey()).isNull();
        assertThat(response.contentUrl()).isNull();
        assertThat(response.likeCount()).isZero();
        assertThat(response.favoriteCount()).isZero();
        assertThat(response.liked()).isFalse();
        assertThat(response.favorited()).isFalse();
        assertThat(response.imageUrls()).containsExactly("https://cdn.noteit.test/article/image/first-1.jpg");
    }

    @Test
    void updateArticleShouldRejectNonAuthor() {
        ArticleDetailResponse response = articleApplicationService.createArticle(2002L, createRequest("before"));

        assertThatThrownBy(() -> articleApplicationService.updateArticle(2003L, response.id(), updateRequest("after")))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ARTICLE_AUTHOR_MISMATCH);
    }

    @Test
    void updateArticleShouldResetSummaryStatusAndReplaceMedia() {
        ArticleDetailResponse response = articleApplicationService.createArticle(2004L, createRequest("before"));
        long articleId = Long.parseLong(response.id());

        jdbcTemplate.update("UPDATE article SET summary_status = 2, summary_text = 'old-summary' WHERE id = ?", articleId);

        ArticleDetailResponse updated = articleApplicationService.updateArticle(2004L, response.id(), updateRequest("after"));

        Integer summaryStatus = jdbcTemplate.queryForObject(
                "SELECT summary_status FROM article WHERE id = ?",
                Integer.class,
                articleId
        );
        String summaryText = jdbcTemplate.queryForObject(
                "SELECT summary_text FROM article WHERE id = ?",
                String.class,
                articleId
        );
        String contentText = jdbcTemplate.queryForObject(
                "SELECT content_text FROM article WHERE id = ?",
                String.class,
                articleId
        );
        Integer mediaCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM article_media WHERE article_id = ?",
                Integer.class,
                articleId
        );

        assertThat(summaryStatus).isEqualTo(0);
        assertThat(summaryText).isNull();
        assertThat(contentText).isEqualTo("# after\nThis is the updated body for after.");
        assertThat(mediaCount).isEqualTo(1);
        assertThat(updated.content()).isEqualTo("# after\nThis is the updated body for after.");
        assertThat(updated.contentObjectKey()).isNull();
        assertThat(updated.imageUrls()).containsExactly("https://cdn.noteit.test/article/image/after-1.jpg");
    }

    @Test
    void createArticleShouldRejectBlankContent() {
        assertThatThrownBy(() -> articleApplicationService.createArticle(2005L, new CreateArticleRequest(
                "Blank body",
                "   ",
                "MARKDOWN",
                null,
                null,
                null,
                List.of()
        )))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ARTICLE_CONTENT_EMPTY);
    }

    @Test
    void getFeedShouldReturnPagedArticlesWithInteractionState() {
        ArticleDetailResponse first = articleApplicationService.createArticle(2101L, createRequest("feed-first"));
        ArticleDetailResponse second = articleApplicationService.createArticle(2102L, createRequest("feed-second"));

        jdbcTemplate.update(
                "INSERT INTO article_like (id, article_id, user_id, status) VALUES (?, ?, ?, 1)",
                900001L,
                Long.parseLong(second.id()),
                8888L
        );
        jdbcTemplate.update(
                "INSERT INTO article_favorite (id, article_id, user_id, status) VALUES (?, ?, ?, 1)",
                900002L,
                Long.parseLong(second.id()),
                8888L
        );

        PageResponse<ArticleCardResponse> feed = articleApplicationService.getFeed(
                new ArticleFeedQuery(1, 10, null),
                8888L
        );

        assertThat(feed.total()).isEqualTo(2);
        assertThat(feed.records()).hasSize(2);
        assertThat(feed.records().get(0).id()).isEqualTo(second.id());
        assertThat(feed.records().get(0).liked()).isTrue();
        assertThat(feed.records().get(0).favorited()).isTrue();
        assertThat(feed.records().get(0).likeCount()).isEqualTo(1);
        assertThat(feed.records().get(1).id()).isEqualTo(first.id());
    }

    @Test
    void getFeedShouldFilterByAuthorId() {
        articleApplicationService.createArticle(2201L, createRequest("author-a"));
        ArticleDetailResponse authorBArticle = articleApplicationService.createArticle(2202L, createRequest("author-b"));

        PageResponse<ArticleCardResponse> feed = articleApplicationService.getFeed(
                new ArticleFeedQuery(1, 10, "2202"),
                null
        );

        assertThat(feed.total()).isEqualTo(1);
        assertThat(feed.records()).extracting(ArticleCardResponse::id).containsExactly(authorBArticle.id());
        assertThat(feed.records().get(0).liked()).isFalse();
        assertThat(feed.records().get(0).favorited()).isFalse();
    }

    @Test
    void getFeedShouldRejectInvalidAuthorId() {
        assertThatThrownBy(() -> articleApplicationService.getFeed(new ArticleFeedQuery(1, 10, "abc"), null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PARAMETER);
    }

    private CreateArticleRequest createRequest(String token) {
        return new CreateArticleRequest(
                "Article " + token,
                "#" + " " + token + "\nThis is the full body for " + token + ".",
                "MARKDOWN",
                "Preview " + token,
                "article/image/2026/04/13/" + token + "-cover.jpg",
                "https://cdn.noteit.test/article/image/" + token + "-cover.jpg",
                List.of(new ArticleImageRequest(
                        "article/image/2026/04/13/" + token + "-1.jpg",
                        "https://cdn.noteit.test/article/image/" + token + "-1.jpg",
                        1,
                        false
                ))
        );
    }

    private UpdateArticleRequest updateRequest(String token) {
        return new UpdateArticleRequest(
                "Updated " + token,
                "#" + " " + token + "\nThis is the updated body for " + token + ".",
                "MARKDOWN",
                "Preview " + token,
                "article/image/2026/04/13/" + token + "-cover.jpg",
                "https://cdn.noteit.test/article/image/" + token + "-cover.jpg",
                List.of(new ArticleImageRequest(
                        "article/image/2026/04/13/" + token + "-1.jpg",
                        "https://cdn.noteit.test/article/image/" + token + "-1.jpg",
                        1,
                        false
                ))
        );
    }
}
