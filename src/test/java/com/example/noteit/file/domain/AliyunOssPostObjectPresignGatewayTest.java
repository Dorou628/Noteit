package com.example.noteit.file.domain;

import com.example.noteit.file.config.OssProperties;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AliyunOssPostObjectPresignGatewayTest {

    @Test
    void presignUploadShouldReturnPostPolicyFields() {
        OssProperties properties = new OssProperties();
        properties.setBucket("noteit-test");
        properties.setAccessKeyId("test-ak");
        properties.setAccessKeySecret("test-sk");
        properties.setPublicBaseUrl("https://noteit-test.oss-cn-test.aliyuncs.com");

        AliyunOssPostObjectPresignGateway gateway = new AliyunOssPostObjectPresignGateway(properties);

        OssPresignResult result = gateway.presignUpload(new OssPresignRequest(
                "article/image/2026/04/18/demo.jpg",
                "image/jpeg",
                1024,
                OffsetDateTime.of(2026, 4, 18, 8, 30, 0, 0, ZoneOffset.ofHours(8))
        ));

        assertThat(result.uploadMethod()).isEqualTo("POST");
        assertThat(result.uploadUrl()).isEqualTo("https://noteit-test.oss-cn-test.aliyuncs.com");
        assertThat(result.publicUrl()).isEqualTo("https://noteit-test.oss-cn-test.aliyuncs.com/article/image/2026/04/18/demo.jpg");
        assertThat(result.formFields()).containsKeys("key", "OSSAccessKeyId", "policy", "Signature", "success_action_status");
        assertThat(result.formFields().get("key")).isEqualTo("article/image/2026/04/18/demo.jpg");
        assertThat(result.formFields().get("OSSAccessKeyId")).isEqualTo("test-ak");
        assertThat(result.formFields().get("success_action_status")).isEqualTo("204");
    }
}
