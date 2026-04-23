package com.example.noteit.relation.controller;

import com.example.noteit.common.auth.CurrentUserResolver;
import com.example.noteit.common.model.PageQuery;
import com.example.noteit.common.response.ApiResponse;
import com.example.noteit.common.response.PageResponse;
import com.example.noteit.relation.model.UserFollowResponse;
import com.example.noteit.relation.service.RelationApplicationService;
import com.example.noteit.user.model.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class RelationController {

    private final RelationApplicationService relationApplicationService;
    private final CurrentUserResolver currentUserResolver;

    public RelationController(
            RelationApplicationService relationApplicationService,
            CurrentUserResolver currentUserResolver
    ) {
        this.relationApplicationService = relationApplicationService;
        this.currentUserResolver = currentUserResolver;
    }

    @PutMapping("/{userId}/follow")
    public ApiResponse<UserFollowResponse> followUser(@PathVariable String userId) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(relationApplicationService.followUser(currentUserId, userId));
    }

    @DeleteMapping("/{userId}/follow")
    public ApiResponse<UserFollowResponse> unfollowUser(@PathVariable String userId) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(relationApplicationService.unfollowUser(currentUserId, userId));
    }

    @GetMapping("/{userId}/following")
    public ApiResponse<PageResponse<UserProfileResponse>> getFollowingUsers(
            @PathVariable String userId,
            @Valid PageQuery pageQuery
    ) {
        Long currentUserId = currentUserResolver.getCurrentUser()
                .map(userContext -> userContext.userId())
                .orElse(null);
        return ApiResponse.success(relationApplicationService.getFollowingUsers(
                userId,
                currentUserId,
                pageQuery.normalizedPageNo(),
                pageQuery.normalizedPageSize()
        ));
    }

    @GetMapping("/{userId}/followers")
    public ApiResponse<PageResponse<UserProfileResponse>> getFollowerUsers(
            @PathVariable String userId,
            @Valid PageQuery pageQuery
    ) {
        Long currentUserId = currentUserResolver.getCurrentUser()
                .map(userContext -> userContext.userId())
                .orElse(null);
        return ApiResponse.success(relationApplicationService.getFollowerUsers(
                userId,
                currentUserId,
                pageQuery.normalizedPageNo(),
                pageQuery.normalizedPageSize()
        ));
    }
}
