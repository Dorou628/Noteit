package com.example.noteit.file.model;

import java.time.LocalDateTime;

public record UploadTaskDO(
        long id,
        long userId,
        String bizType,
        String fileName,
        String contentType,
        long contentLength,
        String objectKey,
        String publicUrl,
        int status,
        String etag,
        LocalDateTime expiredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
