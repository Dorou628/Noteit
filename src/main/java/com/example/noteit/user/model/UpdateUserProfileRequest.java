package com.example.noteit.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank @Size(max = 64) String nickname,
        @Size(max = 512) String avatarUrl,
        @Size(max = 255) String bio
) {
}
