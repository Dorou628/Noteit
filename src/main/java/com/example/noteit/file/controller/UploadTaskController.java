package com.example.noteit.file.controller;

import com.example.noteit.common.auth.CurrentUserResolver;
import com.example.noteit.common.response.ApiResponse;
import com.example.noteit.file.model.CompleteUploadTaskRequest;
import com.example.noteit.file.model.CreateUploadTaskRequest;
import com.example.noteit.file.model.UploadTaskResponse;
import com.example.noteit.file.service.UploadTaskApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/upload-tasks")
public class UploadTaskController {

    private final UploadTaskApplicationService uploadTaskApplicationService;
    private final CurrentUserResolver currentUserResolver;

    public UploadTaskController(
            UploadTaskApplicationService uploadTaskApplicationService,
            CurrentUserResolver currentUserResolver
    ) {
        this.uploadTaskApplicationService = uploadTaskApplicationService;
        this.currentUserResolver = currentUserResolver;
    }

    @PostMapping
    public ApiResponse<UploadTaskResponse> createUploadTask(@Valid @RequestBody CreateUploadTaskRequest request) {
        // 当前仍然使用请求头模拟登录，但统一通过 CurrentUserResolver 取用户，方便后续切 JWT。
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(uploadTaskApplicationService.createUploadTask(currentUserId, request));
    }

    @PostMapping("/{uploadTaskId}/complete")
    public ApiResponse<UploadTaskResponse> completeUploadTask(
            @PathVariable String uploadTaskId,
            @RequestBody(required = false) CompleteUploadTaskRequest request
    ) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        // 允许前端不传 body，避免“只确认完成、不回传 etag”时还要额外拼空 JSON。
        CompleteUploadTaskRequest safeRequest = request == null ? new CompleteUploadTaskRequest(null) : request;
        return ApiResponse.success(uploadTaskApplicationService.completeUploadTask(currentUserId, uploadTaskId, safeRequest));
    }
}
