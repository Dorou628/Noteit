package com.example.noteit.common.constant;

public enum ErrorCode {
    SUCCESS("0", "success"),
    UNAUTHORIZED("UNAUTHORIZED", "User is not authenticated"),
    AUTH_BAD_CREDENTIALS("AUTH_BAD_CREDENTIALS", "Username or password is incorrect"),
    FORBIDDEN("FORBIDDEN", "Operation is forbidden"),
    INVALID_PARAMETER("INVALID_PARAMETER", "Request parameter is invalid"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested resource was not found"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error"),
    ARTICLE_NOT_FOUND("ARTICLE_NOT_FOUND", "Article was not found"),
    ARTICLE_TITLE_EMPTY("ARTICLE_TITLE_EMPTY", "Article title cannot be empty"),
    ARTICLE_CONTENT_EMPTY("ARTICLE_CONTENT_EMPTY", "Article content cannot be empty"),
    ARTICLE_AUTHOR_MISMATCH("ARTICLE_AUTHOR_MISMATCH", "Only the author can update the article"),
    UPLOAD_TASK_NOT_FOUND("UPLOAD_TASK_NOT_FOUND", "Upload task was not found"),
    FOLLOW_SELF_NOT_ALLOWED("FOLLOW_SELF_NOT_ALLOWED", "Cannot follow yourself");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
