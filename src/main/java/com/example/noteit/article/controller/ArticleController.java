package com.example.noteit.article.controller;

import com.example.noteit.article.model.ArticleCardResponse;
import com.example.noteit.article.model.ArticleDetailResponse;
import com.example.noteit.article.model.ArticleFeedQuery;
import com.example.noteit.article.model.CreateArticleRequest;
import com.example.noteit.article.model.UpdateArticleRequest;
import com.example.noteit.article.service.ArticleApplicationService;
import com.example.noteit.common.auth.CurrentUserResolver;
import com.example.noteit.common.response.ApiResponse;
import com.example.noteit.common.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleApplicationService articleApplicationService;
    private final CurrentUserResolver currentUserResolver;

    public ArticleController(
            ArticleApplicationService articleApplicationService,
            CurrentUserResolver currentUserResolver
    ) {
        this.articleApplicationService = articleApplicationService;
        this.currentUserResolver = currentUserResolver;
    }

    /**
     * 作用：获取首页文章 Feed。
     * 输入：query 为分页和筛选参数；当前用户可为空。
     * 输出：返回文章卡片分页结果，登录时卡片会带当前用户互动状态。
     */
    @GetMapping
    public ApiResponse<PageResponse<ArticleCardResponse>> getFeed(@Valid ArticleFeedQuery query) {
        Long currentUserId = currentUserResolver.getCurrentUser()
                .map(userContext -> userContext.userId())
                .orElse(null);
        return ApiResponse.success(articleApplicationService.getFeed(query, currentUserId));
    }

    /**
     * 作用：获取文章详情，并在已登录时附带当前用户的互动状态。
     * 输入：articleId 为路径中的文章 ID；当前用户可为空。
     * 输出：返回文章详情、互动计数以及当前用户的点赞/收藏状态。
     */
    @GetMapping("/{articleId}")
    public ApiResponse<ArticleDetailResponse> getArticleDetail(@PathVariable String articleId) {
        Long currentUserId = currentUserResolver.getCurrentUser()
                .map(userContext -> userContext.userId())
                .orElse(null);
        return ApiResponse.success(articleApplicationService.getArticleDetail(articleId, currentUserId));
    }

    /**
     * 作用：创建新文章。
     * 输入：request 为文章创建请求体；当前用户从请求头解析。
     * 输出：返回新建后的文章详情。
     */
    @PostMapping
    public ApiResponse<ArticleDetailResponse> createArticle(@Valid @RequestBody CreateArticleRequest request) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(articleApplicationService.createArticle(currentUserId, request));
    }

    /**
     * 作用：更新已有文章。
     * 输入：articleId 为路径中的文章 ID，request 为更新请求体，当前用户从请求头解析。
     * 输出：返回更新后的文章详情。
     */
    @PatchMapping("/{articleId}")
    public ApiResponse<ArticleDetailResponse> updateArticle(
            @PathVariable String articleId,
            @Valid @RequestBody UpdateArticleRequest request
    ) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        return ApiResponse.success(articleApplicationService.updateArticle(currentUserId, articleId, request));
    }

    /**
     * 作用：删除已有文章。
     * 输入：articleId 为路径中的文章 ID，当前用户从请求头解析。
     * 输出：无；删除后文章会从公开 Feed 和关注 Feed 中移除。
     */
    @DeleteMapping("/{articleId}")
    public ApiResponse<Void> deleteArticle(@PathVariable String articleId) {
        Long currentUserId = currentUserResolver.getRequiredUserId();
        articleApplicationService.deleteArticle(currentUserId, articleId);
        return ApiResponse.success(null);
    }
}
