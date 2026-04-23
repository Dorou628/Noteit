package com.example.noteit.user.controller;

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
class UserControllerIntegrationTest {

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
    void getUserProfileShouldReturnRealProfile() throws Exception {
        createUserProfile("4001", "ProfileUser", "https://cdn.noteit.test/avatar/profile.jpg", "Profile bio", 7, 3, 2);

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/4001"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode data = objectMapper.readTree(response.body()).path("data");
        assertThat(data.path("id").asText()).isEqualTo("4001");
        assertThat(data.path("nickname").asText()).isEqualTo("ProfileUser");
        assertThat(data.path("avatarUrl").asText()).isEqualTo("https://cdn.noteit.test/avatar/profile.jpg");
        assertThat(data.path("bio").asText()).isEqualTo("Profile bio");
        assertThat(data.path("followerCount").asLong()).isEqualTo(7);
        assertThat(data.path("followingCount").asLong()).isEqualTo(3);
        assertThat(data.path("articleCount").asLong()).isEqualTo(2);
        assertThat(data.path("followed").asBoolean()).isFalse();
    }

    @Test
    void getUserProfileShouldReturnNotFoundWhenMissing() throws Exception {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/4999"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("RESOURCE_NOT_FOUND");
    }

    @Test
    void getUserProfileShouldRejectInvalidUserId() throws Exception {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/not-a-number"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("INVALID_PARAMETER");
    }

    @Test
    void updateMyProfileShouldPersistBasicProfileFields() throws Exception {
        createUserProfile("4011", "BeforeName", "https://cdn.noteit.test/avatar/before.jpg", "Before bio", 5, 4, 3);

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/me/profile"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("X-User-Id", "4011")
                        .PUT(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "nickname": "  AfterName  ",
                                  "avatarUrl": "https://cdn.noteit.test/avatar/after.jpg",
                                  "bio": "  After bio  "
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode data = objectMapper.readTree(response.body()).path("data");
        assertThat(data.path("id").asText()).isEqualTo("4011");
        assertThat(data.path("nickname").asText()).isEqualTo("AfterName");
        assertThat(data.path("avatarUrl").asText()).isEqualTo("https://cdn.noteit.test/avatar/after.jpg");
        assertThat(data.path("bio").asText()).isEqualTo("After bio");
        assertThat(data.path("followerCount").asLong()).isEqualTo(5);
        assertThat(data.path("followingCount").asLong()).isEqualTo(4);
        assertThat(data.path("articleCount").asLong()).isEqualTo(3);

        JsonNode profile = objectMapper.readTree(get("/api/v1/users/4011", null).body()).path("data");
        assertThat(profile.path("nickname").asText()).isEqualTo("AfterName");
        assertThat(profile.path("bio").asText()).isEqualTo("After bio");
    }

    @Test
    void updateMyProfileShouldAllowClearingOptionalFields() throws Exception {
        createUserProfile("4012", "Clearable", "https://cdn.noteit.test/avatar/clear.jpg", "Clear bio", 0, 0, 0);

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/me/profile"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("X-User-Id", "4012")
                        .PUT(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "nickname": "Clearable New",
                                  "avatarUrl": "",
                                  "bio": ""
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode data = objectMapper.readTree(response.body()).path("data");
        assertThat(data.path("nickname").asText()).isEqualTo("Clearable New");
        assertThat(data.has("avatarUrl")).isFalse();
        assertThat(data.has("bio")).isFalse();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT avatar_url FROM user_profile WHERE id = 4012",
                String.class
        )).isNull();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT bio FROM user_profile WHERE id = 4012",
                String.class
        )).isNull();
    }

    @Test
    void updateMyProfileShouldRequireLogin() throws Exception {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/me/profile"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .PUT(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "nickname": "No Login",
                                  "avatarUrl": null,
                                  "bio": null
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("UNAUTHORIZED");
    }

    @Test
    void updateMyProfileShouldRejectBlankNickname() throws Exception {
        createUserProfile("4013", "ValidName", null, null, 0, 0, 0);

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/me/profile"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("X-User-Id", "4013")
                        .PUT(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "nickname": "   ",
                                  "avatarUrl": null,
                                  "bio": null
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("INVALID_PARAMETER");
    }

    @Test
    void updateMyProfileShouldReturnNotFoundWhenProfileMissing() throws Exception {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/me/profile"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("X-User-Id", "4998")
                        .PUT(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "nickname": "Missing",
                                  "avatarUrl": null,
                                  "bio": null
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("RESOURCE_NOT_FOUND");
    }

    @Test
    void getPublishedArticlesShouldReturnRealAuthorArticles() throws Exception {
        String otherArticleId = createArticle("Other Article", "other", "4102", "OtherAuthor");
        String firstArticleId = createArticle("Published First", "first", "4101", "PublishedAuthor");
        String secondArticleId = createArticle("Published Second", "second", "4101", "PublishedAuthor");

        likeArticle("4999", secondArticleId);

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/4101/articles?pageNo=1&pageSize=10"))
                        .header("X-User-Id", "4999")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode data = objectMapper.readTree(response.body()).path("data");
        assertThat(data.path("total").asLong()).isEqualTo(2);
        assertThat(data.path("records")).hasSize(2);
        assertThat(data.path("records").get(0).path("id").asText()).isEqualTo(secondArticleId);
        assertThat(data.path("records").get(0).path("liked").asBoolean()).isTrue();
        assertThat(data.path("records").get(0).path("author").path("id").asText()).isEqualTo("4101");
        assertThat(data.path("records").get(1).path("id").asText()).isEqualTo(firstArticleId);
        assertThat(data.toString()).doesNotContain(otherArticleId);
    }

    @Test
    void getMyLikedArticlesShouldReturnOnlyActiveLikedArticles() throws Exception {
        String firstArticleId = createArticle("Liked First", "liked-first", "4201", "AuthorA");
        String secondArticleId = createArticle("Liked Second", "liked-second", "4202", "AuthorB");
        String unlikedArticleId = createArticle("Unliked", "unliked", "4203", "AuthorC");

        likeArticle("5001", firstArticleId);
        likeArticle("5001", secondArticleId);
        likeArticle("5001", unlikedArticleId);
        unlikeArticle("5001", unlikedArticleId);
        favoriteArticle("5001", secondArticleId);

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/me/liked-articles?pageNo=1&pageSize=10"))
                        .header("X-User-Id", "5001")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode data = objectMapper.readTree(response.body()).path("data");
        assertThat(data.path("total").asLong()).isEqualTo(2);
        assertThat(data.path("records")).hasSize(2);
        assertThat(data.path("records").get(0).path("id").asText()).isEqualTo(secondArticleId);
        assertThat(data.path("records").get(0).path("liked").asBoolean()).isTrue();
        assertThat(data.path("records").get(0).path("favorited").asBoolean()).isTrue();
        assertThat(data.path("records").get(1).path("id").asText()).isEqualTo(firstArticleId);
        assertThat(data.toString()).doesNotContain(unlikedArticleId);
    }

    @Test
    void getMyFavoritedArticlesShouldReturnOnlyActiveFavoritedArticles() throws Exception {
        String firstArticleId = createArticle("Favorite First", "favorite-first", "4301", "AuthorA");
        String secondArticleId = createArticle("Favorite Second", "favorite-second", "4302", "AuthorB");
        String removedArticleId = createArticle("Removed Favorite", "favorite-removed", "4303", "AuthorC");

        favoriteArticle("5002", firstArticleId);
        favoriteArticle("5002", secondArticleId);
        favoriteArticle("5002", removedArticleId);
        unfavoriteArticle("5002", removedArticleId);
        likeArticle("5002", secondArticleId);

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/me/favorited-articles?pageNo=1&pageSize=10"))
                        .header("X-User-Id", "5002")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode data = objectMapper.readTree(response.body()).path("data");
        assertThat(data.path("total").asLong()).isEqualTo(2);
        assertThat(data.path("records")).hasSize(2);
        assertThat(data.path("records").get(0).path("id").asText()).isEqualTo(secondArticleId);
        assertThat(data.path("records").get(0).path("favorited").asBoolean()).isTrue();
        assertThat(data.path("records").get(0).path("liked").asBoolean()).isTrue();
        assertThat(data.path("records").get(1).path("id").asText()).isEqualTo(firstArticleId);
        assertThat(data.toString()).doesNotContain(removedArticleId);
    }

    @Test
    void getMyLikedArticlesShouldRequireLogin() throws Exception {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/me/liked-articles"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("UNAUTHORIZED");
    }

    private String createArticle(String title, String token, String userId, String nickname) throws Exception {
        HttpResponse<String> response = httpClient.send(
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
                                  "coverObjectKey": "article/image/2026/04/23/%s-cover.jpg",
                                  "coverUrl": "https://cdn.noteit.test/article/image/%s-cover.jpg"
                                }
                                """.formatted(title, token, token, token, token)))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readTree(response.body()).path("data").path("id").asText();
    }

    private void likeArticle(String userId, String articleId) throws Exception {
        sendNoBody("PUT", "/api/v1/articles/" + articleId + "/like", userId);
    }

    private void unlikeArticle(String userId, String articleId) throws Exception {
        sendNoBody("DELETE", "/api/v1/articles/" + articleId + "/like", userId);
    }

    private void favoriteArticle(String userId, String articleId) throws Exception {
        sendNoBody("PUT", "/api/v1/articles/" + articleId + "/favorite", userId);
    }

    private void unfavoriteArticle(String userId, String articleId) throws Exception {
        sendNoBody("DELETE", "/api/v1/articles/" + articleId + "/favorite", userId);
    }

    private void sendNoBody(String method, String path, String userId) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("X-User-Id", userId);
        HttpRequest request = switch (method) {
            case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.noBody()).build();
            case "DELETE" -> builder.DELETE().build();
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
    }

    private HttpResponse<String> get(String path, String userId) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .GET();
        if (userId != null) {
            builder.header("X-User-Id", userId);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void createUserProfile(
            String userId,
            String nickname,
            String avatarUrl,
            String bio,
            long followerCount,
            long followingCount,
            long articleCount
    ) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_profile (
                            id, nickname, avatar_url, bio, follower_count, following_count, article_count, status, version
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, 1, 0)
                        """,
                Long.parseLong(userId),
                nickname,
                avatarUrl,
                bio,
                followerCount,
                followingCount,
                articleCount
        );
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
