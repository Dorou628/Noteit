package com.example.noteit.article.controller;

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
class ArticleControllerIntegrationTest {

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
    void createAndGetArticleShouldReturnPersistedData() throws Exception {
        String createPayload = """
                {
                  "title": "Controller Article",
                  "content": "# Controller\\nThis is controller content.",
                  "contentFormat": "MARKDOWN",
                  "coverObjectKey": "article/image/2026/04/13/controller-cover.jpg",
                  "coverUrl": "https://cdn.noteit.test/article/image/controller-cover.jpg",
                  "images": [
                    {
                      "objectKey": "article/image/2026/04/13/controller-1.jpg",
                      "url": "https://cdn.noteit.test/article/image/controller-1.jpg",
                      "sortNo": 1,
                      "cover": false
                    }
                  ]
                }
                """;

        HttpResponse<String> createResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("X-User-Id", "3001")
                        .header("X-User-Nickname", "Tester")
                        .POST(HttpRequest.BodyPublishers.ofString(createPayload))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(createResponse.statusCode())
                .withFailMessage("create article failed, response body: %s", createResponse.body())
                .isEqualTo(200);
        JsonNode created = objectMapper.readTree(createResponse.body()).path("data");
        String articleId = created.path("id").asText();
        assertThat(created.path("title").asText()).isEqualTo("Controller Article");
        assertThat(created.path("content").asText()).isEqualTo("# Controller\nThis is controller content.");
        assertThat(created.path("contentObjectKey").isNull()).isTrue();
        assertThat(created.path("summary").path("status").asText()).isEqualTo("PENDING");

        HttpResponse<String> getResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles/" + articleId))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(getResponse.statusCode()).isEqualTo(200);
        JsonNode detail = objectMapper.readTree(getResponse.body()).path("data");
        assertThat(detail.path("id").asText()).isEqualTo(articleId);
        assertThat(detail.path("author").path("nickname").asText()).isEqualTo("Tester");
        assertThat(detail.path("content").asText()).isEqualTo("# Controller\nThis is controller content.");
        assertThat(detail.path("imageUrls").get(0).asText()).isEqualTo("https://cdn.noteit.test/article/image/controller-1.jpg");
        assertThat(detail.path("likeCount").asLong()).isEqualTo(0);
        assertThat(detail.path("favoriteCount").asLong()).isEqualTo(0);
        assertThat(detail.path("liked").asBoolean()).isFalse();
        assertThat(detail.path("favorited").asBoolean()).isFalse();
    }

    @Test
    void getFeedShouldReturnPagedCardsWithLoginInteractionState() throws Exception {
        String firstArticleId = createArticle("Feed First", "feed-first", "3101", "FeedAuthorA");
        String secondArticleId = createArticle("Feed Second", "feed-second", "3102", "FeedAuthorB");

        httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles/" + secondArticleId + "/like"))
                        .header("X-User-Id", "3999")
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        HttpResponse<String> feedResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles?pageNo=1&pageSize=2"))
                        .header("X-User-Id", "3999")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(feedResponse.statusCode()).isEqualTo(200);
        JsonNode feed = objectMapper.readTree(feedResponse.body()).path("data");
        assertThat(feed.path("total").asLong()).isEqualTo(2);
        assertThat(feed.path("records")).hasSize(2);
        assertThat(feed.path("records").get(0).path("id").asText()).isEqualTo(secondArticleId);
        assertThat(feed.path("records").get(0).path("liked").asBoolean()).isTrue();
        assertThat(feed.path("records").get(0).path("likeCount").asLong()).isEqualTo(1);
        assertThat(feed.path("records").get(1).path("id").asText()).isEqualTo(firstArticleId);
    }

    @Test
    void getFeedShouldSupportAuthorFilter() throws Exception {
        createArticle("Author A", "author-a", "3201", "AuthorA");
        String authorBArticleId = createArticle("Author B", "author-b", "3202", "AuthorB");

        HttpResponse<String> feedResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles?pageNo=1&pageSize=10&authorId=3202"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(feedResponse.statusCode()).isEqualTo(200);
        JsonNode feed = objectMapper.readTree(feedResponse.body()).path("data");
        assertThat(feed.path("total").asLong()).isEqualTo(1);
        assertThat(feed.path("records")).hasSize(1);
        assertThat(feed.path("records").get(0).path("id").asText()).isEqualTo(authorBArticleId);
        assertThat(feed.path("records").get(0).path("author").path("nickname").asText()).isEqualTo("AuthorB");
    }

    @Test
    void createArticleShouldReturnBadRequestWhenContentMissing() throws Exception {
        HttpResponse<String> createResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("X-User-Id", "3002")
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "title": "Invalid",
                                  "contentFormat": "MARKDOWN"
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(createResponse.statusCode()).isEqualTo(400);
        assertThat(createResponse.body()).contains("INVALID_PARAMETER");
    }

    @Test
    void createArticleShouldReturnUnauthorizedWhenUserHeaderMissing() throws Exception {
        HttpResponse<String> createResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "title": "Unauthorized",
                                  "content": "body",
                                  "contentFormat": "MARKDOWN"
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(createResponse.statusCode()).isEqualTo(401);
        assertThat(createResponse.body()).contains("UNAUTHORIZED");
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private String createArticle(String title, String token, String userId, String nickname) throws Exception {
        HttpResponse<String> createResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("X-User-Id", userId)
                        .header("X-User-Nickname", nickname)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "title": "%s",
                                  "content": "正文-%s",
                                  "contentFormat": "MARKDOWN",
                                  "contentPreview": "预览-%s",
                                  "coverObjectKey": "article/image/2026/04/22/%s-cover.jpg",
                                  "coverUrl": "https://cdn.noteit.test/article/image/%s-cover.jpg"
                                }
                                """.formatted(title, token, token, token, token)))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(createResponse.statusCode()).isEqualTo(200);
        return objectMapper.readTree(createResponse.body()).path("data").path("id").asText();
    }
}
