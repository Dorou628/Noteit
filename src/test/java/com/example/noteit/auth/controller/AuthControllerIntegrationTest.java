package com.example.noteit.auth.controller;

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
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM auth_user");
        jdbcTemplate.update("DELETE FROM user_follow");
        jdbcTemplate.update("DELETE FROM user_profile");
        jdbcTemplate.update("""
                INSERT INTO user_profile (
                    id, nickname, avatar_url, bio, follower_count, following_count, article_count, status, version
                ) VALUES (
                    101, 'Login Tester', 'https://cdn.noteit.test/avatar/login.jpg', 'login bio', 0, 0, 0, 1, 0
                )
                """);
        jdbcTemplate.update("""
                INSERT INTO auth_user (
                    id, user_id, username, password_plain_text, status
                ) VALUES (
                    501, 101, 'login_tester', '123456', 1
                )
                """);
    }

    @Test
    void loginShouldReturnHeaderMockUserContext() throws Exception {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/auth/login"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "username": "login_tester",
                                  "password": "123456"
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode data = objectMapper.readTree(response.body()).path("data");
        assertThat(data.path("userId").asText()).isEqualTo("101");
        assertThat(data.path("nickname").asText()).isEqualTo("Login Tester");
        assertThat(data.path("avatarUrl").asText()).isEqualTo("https://cdn.noteit.test/avatar/login.jpg");
        assertThat(data.path("authMode").asText()).isEqualTo("HEADER_MOCK");
        assertThat(data.has("accessToken")).isFalse();
        assertThat(data.has("refreshToken")).isFalse();
    }

    @Test
    void loginShouldReturnUnauthorizedWhenPasswordWrong() throws Exception {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/auth/login"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "username": "login_tester",
                                  "password": "wrong"
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("AUTH_BAD_CREDENTIALS");
    }

    @Test
    void loginShouldReturnBadRequestWhenUsernameMissing() throws Exception {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/auth/login"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "password": "123456"
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("INVALID_PARAMETER");
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
