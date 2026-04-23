package com.example.noteit.file.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateUploadTaskRequest(
        @NotNull UploadBizType bizType,
        @NotBlank String fileName,
        @NotBlank String contentType,
        @NotNull @Positive Long contentLength
) {
}
