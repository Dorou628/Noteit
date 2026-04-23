package com.example.noteit.file.model;

import java.time.OffsetDateTime;
import java.util.Map;

public record UploadTaskResponse(
        String uploadTaskId,
        String objectKey,
        String uploadMethod,
        String uploadUrl,
        Map<String, String> headers,
        Map<String, String> formFields,
        OffsetDateTime expiredAt,
        String publicUrl,
        String status
) {
}
