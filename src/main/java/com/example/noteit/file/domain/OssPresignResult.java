package com.example.noteit.file.domain;

import java.time.OffsetDateTime;
import java.util.Map;

public record OssPresignResult(
        String uploadMethod,
        String uploadUrl,
        Map<String, String> headers,
        Map<String, String> formFields,
        OffsetDateTime expiredAt,
        String publicUrl
) {
}
