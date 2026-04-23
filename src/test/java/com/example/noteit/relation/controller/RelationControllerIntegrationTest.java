package com.example.noteit.relation.controller;

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
class RelationControllerIntegrationTest {

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
        jdbcTemplate.update("DELETE FROM auth_user");
        jdbcTemplate.update("DELETE FROM user_profile");
    }

    @Test
    void followShouldBeIdempotentAndVisibleOnUserProfile() throws Exception {
        createUserProfile(6101, "Follower");
        createUserProfile(6102, "Followee");

        HttpResponse<String> first = follow("6101", "6102");
        HttpResponse<String> second = follow("6101", "6102");

        assertThat(first.statusCode()).isEqualTo(200);
        assertThat(second.statusCode()).isEqualTo(200);
        JsonNode data = objectMapper.readTree(second.body()).path("data");
        assertThat(data.path("userId").asText()).isEqualTo("6102");
        assertThat(data.path("followed").asBoolean()).isTrue();

        assertThat(countActiveFollows(6101, 6102)).isEqualTo(1);
        assertThat(queryProfileCount(6101, "following_count")).isEqualTo(1);
        assertThat(queryProfileCount(6102, "follower_count")).isEqualTo(1);

        HttpResponse<String> profileResponse = get("/api/v1/users/6102", "6101");
        assertThat(profileResponse.statusCode()).isEqualTo(200);
        JsonNode profile = objectMapper.readTree(profileResponse.body()).path("data");
        assertThat(profile.path("followed").asBoolean()).isTrue();
        assertThat(profile.path("followerCount").asLong()).isEqualTo(1);
    }

    @Test
    void unfollowShouldBeIdempotentAndResetCounters() throws Exception {
        createUserProfile(6111, "Follower");
        createUserProfile(6112, "Followee");
        follow("6111", "6112");

        HttpResponse<String> first = unfollow("6111", "6112");
        HttpResponse<String> second = unfollow("6111", "6112");

        assertThat(first.statusCode()).isEqualTo(200);
        assertThat(second.statusCode()).isEqualTo(200);
        JsonNode data = objectMapper.readTree(second.body()).path("data");
        assertThat(data.path("followed").asBoolean()).isFalse();

        assertThat(countActiveFollows(6111, 6112)).isZero();
        assertThat(queryProfileCount(6111, "following_count")).isZero();
        assertThat(queryProfileCount(6112, "follower_count")).isZero();

        HttpResponse<String> profileResponse = get("/api/v1/users/6112", "6111");
        assertThat(objectMapper.readTree(profileResponse.body()).path("data").path("followed").asBoolean()).isFalse();
    }

    @Test
    void followShouldBeVisibleOnArticleAuthorView() throws Exception {
        createUserProfile(6121, "Reader");
        createUserProfile(6122, "Author");
        String articleId = createArticle("6122", "Author");

        follow("6121", "6122");

        HttpResponse<String> detailResponse = get("/api/v1/articles/" + articleId, "6121");
        assertThat(detailResponse.statusCode()).isEqualTo(200);
        JsonNode detail = objectMapper.readTree(detailResponse.body()).path("data");
        assertThat(detail.path("author").path("followed").asBoolean()).isTrue();

        HttpResponse<String> feedResponse = get("/api/v1/articles?pageNo=1&pageSize=10", "6121");
        assertThat(feedResponse.statusCode()).isEqualTo(200);
        JsonNode firstCard = objectMapper.readTree(feedResponse.body()).path("data").path("records").get(0);
        assertThat(firstCard.path("author").path("followed").asBoolean()).isTrue();
    }

    @Test
    void followShouldRejectSelfAndMissingUser() throws Exception {
        createUserProfile(6131, "Self");

        HttpResponse<String> selfResponse = follow("6131", "6131");
        assertThat(selfResponse.statusCode()).isEqualTo(400);
        assertThat(selfResponse.body()).contains("FOLLOW_SELF_NOT_ALLOWED");

        HttpResponse<String> missingResponse = follow("6131", "6999");
        assertThat(missingResponse.statusCode()).isEqualTo(404);
        assertThat(missingResponse.body()).contains("RESOURCE_NOT_FOUND");
    }

    @Test
    void followShouldRequireLogin() throws Exception {
        createUserProfile(6142, "Followee");

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/users/6142/follow"))
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("UNAUTHORIZED");
    }

    @Test
    void getFollowingUsersShouldReturnPagedProfilesWithViewerFollowState() throws Exception {
        createUserProfile(6201, "Owner");
        createUserProfile(6202, "FollowedA");
        createUserProfile(6203, "FollowedB");
        createUserProfile(6204, "Viewer");
        follow("6201", "6202");
        follow("6201", "6203");
        follow("6204", "6203");

        HttpResponse<String> response = get("/api/v1/users/6201/following?pageNo=1&pageSize=10", "6204");

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode data = objectMapper.readTree(response.body()).path("data");
        assertThat(data.path("total").asLong()).isEqualTo(2);
        assertThat(data.path("records")).hasSize(2);

        JsonNode followedA = findRecordById(data.path("records"), "6202");
        JsonNode followedB = findRecordById(data.path("records"), "6203");
        assertThat(followedA).isNotNull();
        assertThat(followedB).isNotNull();
        assertThat(followedA.path("nickname").asText()).isEqualTo("FollowedA");
        assertThat(followedA.path("followed").asBoolean()).isFalse();
        assertThat(followedB.path("nickname").asText()).isEqualTo("FollowedB");
        assertThat(followedB.path("followed").asBoolean()).isTrue();
    }

    @Test
    void getFollowerUsersShouldReturnOnlyActiveFollowers() throws Exception {
        createUserProfile(6211, "Owner");
        createUserProfile(6212, "FollowerA");
        createUserProfile(6213, "FollowerB");
        createUserProfile(6214, "RemovedFollower");
        createUserProfile(6215, "Viewer");
        follow("6212", "6211");
        follow("6213", "6211");
        follow("6214", "6211");
        unfollow("6214", "6211");
        follow("6215", "6212");

        HttpResponse<String> response = get("/api/v1/users/6211/followers?pageNo=1&pageSize=10", "6215");

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode data = objectMapper.readTree(response.body()).path("data");
        assertThat(data.path("total").asLong()).isEqualTo(2);
        assertThat(data.path("records")).hasSize(2);

        JsonNode followerA = findRecordById(data.path("records"), "6212");
        JsonNode followerB = findRecordById(data.path("records"), "6213");
        assertThat(followerA).isNotNull();
        assertThat(followerB).isNotNull();
        assertThat(followerA.path("followed").asBoolean()).isTrue();
        assertThat(followerB.path("followed").asBoolean()).isFalse();
        assertThat(findRecordById(data.path("records"), "6214")).isNull();
    }

    @Test
    void followListsShouldReturnNotFoundWhenUserMissing() throws Exception {
        HttpResponse<String> followingResponse = get("/api/v1/users/6999/following", null);
        HttpResponse<String> followerResponse = get("/api/v1/users/6999/followers", null);

        assertThat(followingResponse.statusCode()).isEqualTo(404);
        assertThat(followingResponse.body()).contains("RESOURCE_NOT_FOUND");
        assertThat(followerResponse.statusCode()).isEqualTo(404);
        assertThat(followerResponse.body()).contains("RESOURCE_NOT_FOUND");
    }

    private HttpResponse<String> follow(String followerId, String followeeId) throws Exception {
        return sendNoBody("PUT", "/api/v1/users/" + followeeId + "/follow", followerId);
    }

    private HttpResponse<String> unfollow(String followerId, String followeeId) throws Exception {
        return sendNoBody("DELETE", "/api/v1/users/" + followeeId + "/follow", followerId);
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

    private HttpResponse<String> sendNoBody(String method, String path, String userId) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header("X-User-Id", userId);
        HttpRequest request = switch (method) {
            case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.noBody()).build();
            case "DELETE" -> builder.DELETE().build();
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String createArticle(String userId, String nickname) throws Exception {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/articles"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("X-User-Id", userId)
                        .header("X-User-Nickname", nickname)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "title": "Followed Author Article",
                                  "content": "Article body for followed author.",
                                  "contentFormat": "MARKDOWN",
                                  "contentPreview": "Article preview"
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readTree(response.body()).path("data").path("id").asText();
    }

    private void createUserProfile(long userId, String nickname) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_profile (
                            id, nickname, avatar_url, bio, follower_count, following_count, article_count, status, version
                        ) VALUES (?, ?, NULL, NULL, 0, 0, 0, 1, 0)
                        """,
                userId,
                nickname
        );
    }

    private int countActiveFollows(long followerId, long followeeId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_follow WHERE follower_id = ? AND followee_id = ? AND status = 1",
                Integer.class,
                followerId,
                followeeId
        );
    }

    private long queryProfileCount(long userId, String columnName) {
        return jdbcTemplate.queryForObject(
                "SELECT " + columnName + " FROM user_profile WHERE id = ?",
                Long.class,
                userId
        );
    }

    private JsonNode findRecordById(JsonNode records, String id) {
        for (JsonNode record : records) {
            if (id.equals(record.path("id").asText())) {
                return record;
            }
        }
        return null;
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
