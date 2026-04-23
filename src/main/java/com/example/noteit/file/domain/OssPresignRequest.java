package com.example.noteit.file.domain;

import java.time.OffsetDateTime;

public record OssPresignRequest(
        String objectKey,
        String contentType,
        long contentLength,
        OffsetDateTime expiredAt
) {
}
