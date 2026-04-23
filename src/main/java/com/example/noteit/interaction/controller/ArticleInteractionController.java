package com.example.noteit.interaction.controller;

import com.example.noteit.common.auth.CurrentUserResolver;
import com.example.noteit.common.response.ApiResponse;
import com.example.noteit.interaction.model.ArticleFavoriteResponse;
import com.example.noteit.interaction.model.ArticleLikeResponse;
import com.example.noteit.interaction.service.InteractionApplicationService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleInteractionController {

    private final InteractionApplicationService interactionApplicationService;
    private final CurrentUserResolver currentUserResolver;

    public ArticleInteractionController(
            InteractionApplicationService interactionApplicationService,
            CurrentUserResolver currentUserResolver
    ) {
        this.interactionApplicationService = interactionApplicationService;
        this.currentUserResolver = currentUserResolver;
    }

    /**
     * 作用：处理文章点赞请求。
     * 输入：articleId 为路径中的文章 ID；当前用户从请求头解析。
     * 输出：返回点赞后的状态和最新点赞数。
     */
    @PutMapping("/{articleId}/like")
    public ApiResponse<ArticleLikeResponse> likeArticle(@PathVariable String articleId) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(interactionApplicationService.likeArticle(currentUserId, articleId));
    }

    /**
     * 作用：处理文章取消点赞请求。
     * 输入：articleId 为路径中的文章 ID；当前用户从请求头解析。
     * 输出：返回取消点赞后的状态和最新点赞数。
     */
    @DeleteMapping("/{articleId}/like")
    public ApiResponse<ArticleLikeResponse> unlikeArticle(@PathVariable String articleId) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(interactionApplicationService.unlikeArticle(currentUserId, articleId));
    }

    /**
     * 作用：处理文章收藏请求。
     * 输入：articleId 为路径中的文章 ID；当前用户从请求头解析。
     * 输出：返回收藏后的状态和最新收藏数。
     */
    @PutMapping("/{articleId}/favorite")
    public ApiResponse<ArticleFavoriteResponse> favoriteArticle(@PathVariable String articleId) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(interactionApplicationService.favoriteArticle(currentUserId, articleId));
    }

    /**
     * 作用：处理文章取消收藏请求。
     * 输入：articleId 为路径中的文章 ID；当前用户从请求头解析。
     * 输出：返回取消收藏后的状态和最新收藏数。
     */
    @DeleteMapping("/{articleId}/favorite")
    public ApiResponse<ArticleFavoriteResponse> unfavoriteArticle(@PathVariable String articleId) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(interactionApplicationService.unfavoriteArticle(currentUserId, articleId));
    }
}
