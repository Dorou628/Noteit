package com.example.noteit.common.response;

import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.context.RequestContextHolder;

public record ApiResponse<T>(
        String code,
        String message,
        String requestId,
        T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                ErrorCode.SUCCESS.code(),
                ErrorCode.SUCCESS.defaultMessage(),
                RequestContextHolder.getRequestId().orElse("N/A"),
                data
        );
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String message) {
        return new ApiResponse<>(
                errorCode.code(),
                message,
                RequestContextHolder.getRequestId().orElse("N/A"),
                null
        );
    }
}
