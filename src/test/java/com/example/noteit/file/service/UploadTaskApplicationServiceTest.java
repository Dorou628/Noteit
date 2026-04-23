package com.example.noteit.file.service;

import com.example.noteit.file.model.CompleteUploadTaskRequest;
import com.example.noteit.file.model.CreateUploadTaskRequest;
import com.example.noteit.file.model.UploadBizType;
import com.example.noteit.file.model.UploadTaskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UploadTaskApplicationServiceTest {

    @Autowired
    private UploadTaskApplicationService uploadTaskApplicationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM upload_task");
    }

    @Test
    void createUploadTaskShouldPersistTaskAndGenerateObjectKey() {
        UploadTaskResponse response = uploadTaskApplicationService.createUploadTask(
                1001L,
                new CreateUploadTaskRequest(UploadBizType.ARTICLE_CONTENT, "note.md", "text/markdown", 128L)
        );

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM upload_task WHERE id = ? AND status = 0",
                Integer.class,
                Long.parseLong(response.uploadTaskId())
        );

        assertThat(count).isEqualTo(1);
        assertThat(response.objectKey()).contains("article/content/");
        assertThat(response.objectKey()).contains(response.uploadTaskId() + "-note.md");
        assertThat(response.uploadMethod()).isEqualTo("POST");
        assertThat(response.uploadUrl()).contains("https://noteit-test.oss-cn-test.aliyuncs.com");
        assertThat(response.formFields()).containsKeys("key", "OSSAccessKeyId", "policy", "Signature", "success_action_status");
        assertThat(response.status()).isEqualTo("CREATED");
    }

    @Test
    void completeUploadTaskShouldMarkTaskConfirmed() {
        UploadTaskResponse created = uploadTaskApplicationService.createUploadTask(
                1002L,
                new CreateUploadTaskRequest(UploadBizType.ARTICLE_IMAGE, "cover.jpg", "image/jpeg", 256L)
        );

        UploadTaskResponse confirmed = uploadTaskApplicationService.completeUploadTask(
                1002L,
                created.uploadTaskId(),
                new CompleteUploadTaskRequest("etag-1")
        );

        Integer status = jdbcTemplate.queryForObject(
                "SELECT status FROM upload_task WHERE id = ?",
                Integer.class,
                Long.parseLong(created.uploadTaskId())
        );

        assertThat(status).isEqualTo(2);
        assertThat(confirmed.status()).isEqualTo("CONFIRMED");
        assertThat(confirmed.uploadUrl()).isNull();
    }
}
