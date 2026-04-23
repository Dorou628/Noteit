package com.example.noteit.auth.model;

import java.time.LocalDateTime;

public record AuthUserDO(
        long id,
        long userId,
        String username,
        String passwordPlainText,
        int status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
