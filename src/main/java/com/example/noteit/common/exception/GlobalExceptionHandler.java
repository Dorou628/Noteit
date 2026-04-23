package com.example.noteit.common.exception;

import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case UNAUTHORIZED, AUTH_BAD_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, ARTICLE_AUTHOR_MISMATCH -> HttpStatus.FORBIDDEN;
            case RESOURCE_NOT_FOUND, ARTICLE_NOT_FOUND, UPLOAD_TASK_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status)
                .body(ApiResponse.failure(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ErrorCode.INVALID_PARAMETER, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknownException(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.failure(ErrorCode.INTERNAL_ERROR, ex.getMessage()));
    }
}
