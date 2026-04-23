package com.example.noteit.file.domain;

import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.exception.BusinessException;
import com.example.noteit.file.config.OssProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AliyunOssPostObjectPresignGateway implements OssPresignGateway {

    private static final DateTimeFormatter OSS_POLICY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final OssProperties ossProperties;

    public AliyunOssPostObjectPresignGateway(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public OssPresignResult presignUpload(OssPresignRequest request) {
        validateConfiguration();

        String normalizedUploadUrl = normalizeBaseUrl(ossProperties.getPublicBaseUrl());
        String normalizedPublicUrl = normalizedUploadUrl + "/" + request.objectKey();
        String expiration = request.expiredAt()
                .withOffsetSameInstant(ZoneOffset.UTC)
                .format(OSS_POLICY_TIME_FORMATTER);
        String policyJson = buildPolicyJson(expiration, request.objectKey());
        String policy = Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));
        String signature = signPolicy(policy);

        Map<String, String> formFields = new LinkedHashMap<>();
        // key 由服务端生成，前端只能按约定位置上传，避免对象路径规则散落到客户端。
        formFields.put("key", request.objectKey());
        formFields.put("OSSAccessKeyId", ossProperties.getAccessKeyId());
        formFields.put("policy", policy);
        formFields.put("Signature", signature);
        formFields.put("success_action_status", "204");

        return new OssPresignResult(
                "POST",
                normalizedUploadUrl,
                Map.of(),
                Map.copyOf(formFields),
                request.expiredAt(),
                normalizedPublicUrl
        );
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(ossProperties.getAccessKeyId())
                || !StringUtils.hasText(ossProperties.getAccessKeySecret())
                || !StringUtils.hasText(ossProperties.getBucket())
                || !StringUtils.hasText(ossProperties.getPublicBaseUrl())) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "OSS configuration is incomplete");
        }
    }

    private String buildPolicyJson(String expiration, String objectKey) {
        // 当前只约束 bucket、key 和成功状态，尽量降低联调复杂度。
        // 后续如果需要更强约束，可以继续追加 content-length-range、Content-Type 等条件。
        return """
                {"expiration":"%s","conditions":[{"bucket":"%s"},{"key":"%s"},{"success_action_status":"204"}]}
                """.formatted(expiration, escapeJson(ossProperties.getBucket()), escapeJson(objectKey));
    }

    private String signPolicy(String policy) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(ossProperties.getAccessKeySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] signed = mac.doFinal(policy.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to sign OSS policy");
        }
    }

    private String normalizeBaseUrl(String rawBaseUrl) {
        String trimmed = rawBaseUrl.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
