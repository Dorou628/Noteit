package com.example.noteit.interaction.service;

import com.example.noteit.article.model.CreateArticleRequest;
import com.example.noteit.article.service.ArticleApplicationService;
import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.exception.BusinessException;
import com.example.noteit.interaction.model.ArticleFavoriteResponse;
import com.example.noteit.interaction.model.ArticleInteractionSnapshot;
import com.example.noteit.interaction.model.ArticleLikeResponse;
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
class InteractionApplicationServiceTest {

    @Autowired
    private InteractionApplicationService interactionApplicationService;

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
    void likeArticleShouldBeIdempotent() {
        String articleId = createArticle("like-case");

        ArticleLikeResponse first = interactionApplicationService.likeArticle(4001L, articleId);
        ArticleLikeResponse second = interactionApplicationService.likeArticle(4001L, articleId);

        Integer activeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM article_like WHERE article_id = ? AND user_id = ? AND status = 1",
                Integer.class,
                Long.parseLong(articleId),
                4001L
        );

        assertThat(first.liked()).isTrue();
        assertThat(first.likeCount()).isEqualTo(1);
        assertThat(second.liked()).isTrue();
        assertThat(second.likeCount()).isEqualTo(1);
        assertThat(activeCount).isEqualTo(1);
    }

    @Test
    void unlikeArticleShouldBeIdempotent() {
        String articleId = createArticle("unlike-case");
        interactionApplicationService.likeArticle(4002L, articleId);

        ArticleLikeResponse first = interactionApplicationService.unlikeArticle(4002L, articleId);
        ArticleLikeResponse second = interactionApplicationService.unlikeArticle(4002L, articleId);

        Integer inactiveCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM article_like WHERE article_id = ? AND user_id = ? AND status = 0",
                Integer.class,
                Long.parseLong(articleId),
                4002L
        );

        assertThat(first.liked()).isFalse();
        assertThat(first.likeCount()).isZero();
        assertThat(second.liked()).isFalse();
        assertThat(second.likeCount()).isZero();
        assertThat(inactiveCount).isEqualTo(1);
    }

    @Test
    void favoriteArticleShouldBeIdempotentAndSnapshotShouldReflectState() {
        String articleId = createArticle("favorite-case");

        ArticleFavoriteResponse favorite = interactionApplicationService.favoriteArticle(4003L, articleId);
        ArticleFavoriteResponse favoriteAgain = interactionApplicationService.favoriteArticle(4003L, articleId);
        ArticleInteractionSnapshot snapshot =
                interactionApplicationService.getInteractionSnapshot(Long.parseLong(articleId), 4003L);

        assertThat(favorite.favorited()).isTrue();
        assertThat(favorite.favoriteCount()).isEqualTo(1);
        assertThat(favoriteAgain.favoriteCount()).isEqualTo(1);
        assertThat(snapshot.favoriteCount()).isEqualTo(1);
        assertThat(snapshot.favorited()).isTrue();
        assertThat(snapshot.liked()).isFalse();
    }

    @Test
    void likeArticleShouldThrowWhenArticleMissing() {
        assertThatThrownBy(() -> interactionApplicationService.likeArticle(4004L, "999999"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ARTICLE_NOT_FOUND);
    }

    private String createArticle(String token) {
        return articleApplicationService.createArticle(5000L, new CreateArticleRequest(
                "Interaction " + token,
                "正文-" + token,
                "MARKDOWN",
                "预览-" + token,
                null,
                null,
                List.of()
        )).id();
    }
}
