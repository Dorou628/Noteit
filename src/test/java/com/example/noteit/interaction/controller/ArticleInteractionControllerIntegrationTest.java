package com.example.noteit.interaction.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ArticleInteractionControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    void likeAndFavoriteShouldBeVisibleInArticleDetail() throws Exception {
        String articleId = createArticle();

        HttpResponse<String> likeResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles/" + articleId + "/like"))
                        .header("X-User-Id", "7001")
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(likeResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> favoriteResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles/" + articleId + "/favorite"))
                        .header("X-User-Id", "7001")
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(favoriteResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> detailResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles/" + articleId))
                        .header("X-User-Id", "7001")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(detailResponse.statusCode()).isEqualTo(200);
        JsonNode detail = objectMapper.readTree(detailResponse.body()).path("data");
        assertThat(detail.path("likeCount").asLong()).isEqualTo(1);
        assertThat(detail.path("favoriteCount").asLong()).isEqualTo(1);
        assertThat(detail.path("liked").asBoolean()).isTrue();
        assertThat(detail.path("favorited").asBoolean()).isTrue();
    }

    @Test
    void unfavoriteShouldResetDetailStateAndAnonymousReadShouldStillWork() throws Exception {
        String articleId = createArticle();

        httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles/" + articleId + "/favorite"))
                        .header("X-User-Id", "7002")
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        HttpResponse<String> unfavoriteResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles/" + articleId + "/favorite"))
                        .header("X-User-Id", "7002")
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(unfavoriteResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> anonymousDetailResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles/" + articleId))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(anonymousDetailResponse.statusCode()).isEqualTo(200);
        JsonNode detail = objectMapper.readTree(anonymousDetailResponse.body()).path("data");
        assertThat(detail.path("favoriteCount").asLong()).isZero();
        assertThat(detail.path("favorited").asBoolean()).isFalse();
        assertThat(detail.path("liked").asBoolean()).isFalse();
    }

    @Test
    void likeShouldReturnUnauthorizedWhenUserHeaderMissing() throws Exception {
        String articleId = createArticle();

        HttpResponse<String> likeResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles/" + articleId + "/like"))
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(likeResponse.statusCode()).isEqualTo(401);
        assertThat(likeResponse.body()).contains("UNAUTHORIZED");
    }

    private String createArticle() throws Exception {
        HttpResponse<String> createResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("X-User-Id", "6001")
                        .header("X-User-Nickname", "InteractionTester")
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "title": "Interaction Detail",
                                  "content": "用于互动联调的正文",
                                  "contentFormat": "MARKDOWN"
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(createResponse.statusCode()).isEqualTo(200);
        return objectMapper.readTree(createResponse.body()).path("data").path("id").asText();
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
