package com.example.noteit.user.controller;

import com.example.noteit.article.model.ArticleCardResponse;
import com.example.noteit.common.auth.CurrentUserResolver;
import com.example.noteit.common.model.PageQuery;
import com.example.noteit.common.response.ApiResponse;
import com.example.noteit.common.response.PageResponse;
import com.example.noteit.user.model.UpdateUserProfileRequest;
import com.example.noteit.user.model.UserProfileResponse;
import com.example.noteit.user.service.UserApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserApplicationService userApplicationService;
    private final CurrentUserResolver currentUserResolver;

    public UserController(
            UserApplicationService userApplicationService,
            CurrentUserResolver currentUserResolver
    ) {
        this.userApplicationService = userApplicationService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getUserProfile(@PathVariable String userId) {
        Long currentUserId = currentUserResolver.getCurrentUser()
                .map(userContext -> userContext.userId())
                .orElse(null);
        return ApiResponse.success(userApplicationService.getUserProfile(userId, currentUserId));
    }

    @PutMapping("/me/profile")
    public ApiResponse<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(userApplicationService.updateMyProfile(currentUserId, request));
    }

    @GetMapping("/{userId}/articles")
    public ApiResponse<PageResponse<ArticleCardResponse>> getPublishedArticles(
            @PathVariable String userId,
            @Valid PageQuery pageQuery
    ) {
        Long currentUserId = currentUserResolver.getCurrentUser()
                .map(userContext -> userContext.userId())
                .orElse(null);
        return ApiResponse.success(userApplicationService.getPublishedArticles(
                userId,
                currentUserId,
                pageQuery.normalizedPageNo(),
                pageQuery.normalizedPageSize()
        ));
    }

    @GetMapping("/me/liked-articles")
    public ApiResponse<PageResponse<ArticleCardResponse>> getMyLikedArticles(@Valid PageQuery pageQuery) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(userApplicationService.getMyLikedArticles(
                currentUserId,
                pageQuery.normalizedPageNo(),
                pageQuery.normalizedPageSize()
        ));
    }

    @GetMapping("/me/feed")
    public ApiResponse<PageResponse<ArticleCardResponse>> getMyFollowingFeed(@Valid PageQuery pageQuery) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(userApplicationService.getMyFollowingFeed(
                currentUserId,
                pageQuery.normalizedPageNo(),
                pageQuery.normalizedPageSize()
        ));
    }

    @GetMapping("/me/favorited-articles")
    public ApiResponse<PageResponse<ArticleCardResponse>> getMyFavoritedArticles(@Valid PageQuery pageQuery) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(userApplicationService.getMyFavoritedArticles(
                currentUserId,
                pageQuery.normalizedPageNo(),
                pageQuery.normalizedPageSize()
        ));
    }
}
