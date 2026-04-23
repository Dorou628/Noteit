package com.example.noteit.file.controller;

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
class UploadTaskControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM upload_task");
    }

    @Test
    void createAndCompleteUploadTaskShouldPersistStatus() throws Exception {
        HttpResponse<String> createResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/upload-tasks"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("X-User-Id", "7001")
                        .header("X-User-Nickname", "Uploader")
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "bizType": "ARTICLE_IMAGE",
                                  "fileName": "sample.jpg",
                                  "contentType": "image/jpeg",
                                  "contentLength": 1024
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(createResponse.statusCode()).isEqualTo(200);
        JsonNode created = objectMapper.readTree(createResponse.body()).path("data");
        String uploadTaskId = created.path("uploadTaskId").asText();
        assertThat(created.path("objectKey").asText()).contains("article/image/");
        assertThat(created.path("uploadMethod").asText()).isEqualTo("POST");
        assertThat(created.path("uploadUrl").asText()).contains("https://noteit-test.oss-cn-test.aliyuncs.com");
        assertThat(created.path("formFields").path("policy").asText()).isNotBlank();
        assertThat(created.path("status").asText()).isEqualTo("CREATED");

        HttpResponse<String> completeResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/upload-tasks/" + uploadTaskId + "/complete"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("X-User-Id", "7001")
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "etag": "etag-upload-test"
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(completeResponse.statusCode()).isEqualTo(200);
        JsonNode completed = objectMapper.readTree(completeResponse.body()).path("data");
        assertThat(completed.path("status").asText()).isEqualTo("CONFIRMED");

        Integer status = jdbcTemplate.queryForObject(
                "SELECT status FROM upload_task WHERE id = ?",
                Integer.class,
                Long.parseLong(uploadTaskId)
        );
        assertThat(status).isEqualTo(2);
    }

    @Test
    void createUploadTaskShouldReturnUnauthorizedWhenUserHeaderMissing() throws Exception {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl() + "/api/v1/upload-tasks"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {
                                  "bizType": "ARTICLE_IMAGE",
                                  "fileName": "sample.jpg",
                                  "contentType": "image/jpeg",
                                  "contentLength": 1024
                                }
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("UNAUTHORIZED");
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
