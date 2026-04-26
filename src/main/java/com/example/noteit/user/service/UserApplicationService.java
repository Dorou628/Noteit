package com.example.noteit.user.service;

import com.example.noteit.article.model.ArticleCardResponse;
import com.example.noteit.article.service.ArticleApplicationService;
import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.exception.BusinessException;
import com.example.noteit.common.response.PageResponse;
import com.example.noteit.interaction.service.InteractionApplicationService;
import com.example.noteit.relation.repository.RelationRepository;
import com.example.noteit.user.model.UpdateUserProfileRequest;
import com.example.noteit.user.model.UserProfileDO;
import com.example.noteit.user.model.UserProfileResponse;
import com.example.noteit.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserApplicationService {

    private final ArticleApplicationService articleApplicationService;
    private final InteractionApplicationService interactionApplicationService;
    private final UserProfileRepository userProfileRepository;
    private final RelationRepository relationRepository;

    public UserApplicationService(
            ArticleApplicationService articleApplicationService,
            InteractionApplicationService interactionApplicationService,
            UserProfileRepository userProfileRepository,
            RelationRepository relationRepository
    ) {
        this.articleApplicationService = articleApplicationService;
        this.interactionApplicationService = interactionApplicationService;
        this.userProfileRepository = userProfileRepository;
        this.relationRepository = relationRepository;
    }

    public UserProfileResponse getUserProfile(String userId, Long currentUserId) {
        long parsedUserId = parseUserId(userId);
        UserProfileDO profile = userProfileRepository.findById(parsedUserId)
                .filter(userProfile -> userProfile.status() == 1)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "User profile was not found"
                ));
        boolean followed = currentUserId != null
                && currentUserId != parsedUserId
                && relationRepository.hasActiveFollow(currentUserId, parsedUserId);

        return new UserProfileResponse(
                String.valueOf(profile.id()),
                profile.nickname(),
                profile.avatarUrl(),
                profile.bio(),
                profile.followerCount(),
                profile.followingCount(),
                profile.articleCount(),
                followed
        );
    }

    public PageResponse<ArticleCardResponse> getPublishedArticles(
            String userId,
            Long currentUserId,
            int pageNo,
            int pageSize
    ) {
        long authorId = parseUserId(userId);
        return articleApplicationService.getPublishedArticles(authorId, currentUserId, pageNo, pageSize);
    }

    public PageResponse<ArticleCardResponse> getMyFollowingFeed(Long currentUserId, int pageNo, int pageSize) {
        ensureActiveUser(currentUserId);
        return articleApplicationService.getFollowingFeed(currentUserId, pageNo, pageSize);
    }

    public PageResponse<ArticleCardResponse> getMyLikedArticles(Long currentUserId, int pageNo, int pageSize) {
        return interactionApplicationService.getLikedArticles(currentUserId, pageNo, pageSize);
    }

    public PageResponse<ArticleCardResponse> getMyFavoritedArticles(Long currentUserId, int pageNo, int pageSize) {
        return interactionApplicationService.getFavoritedArticles(currentUserId, pageNo, pageSize);
    }

    @Transactional
    public UserProfileResponse updateMyProfile(Long currentUserId, UpdateUserProfileRequest request) {
        String nickname = request.nickname().trim();
        String avatarUrl = trimToNull(request.avatarUrl());
        String bio = trimToNull(request.bio());

        boolean updated = userProfileRepository.updateBasicProfile(currentUserId, nickname, avatarUrl, bio);
        if (!updated) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User profile was not found");
        }
        return getUserProfile(String.valueOf(currentUserId), currentUserId);
    }

    private long parseUserId(String rawUserId) {
        try {
            return Long.parseLong(rawUserId);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "userId must be a number");
        }
    }

    private UserProfileDO ensureActiveUser(long userId) {
        return userProfileRepository.findById(userId)
                .filter(user -> user.status() == 1)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "User profile was not found"
                ));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
