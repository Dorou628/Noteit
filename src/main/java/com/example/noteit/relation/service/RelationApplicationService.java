package com.example.noteit.relation.service;

import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.event.DomainEventPublisher;
import com.example.noteit.common.exception.BusinessException;
import com.example.noteit.common.id.IdGenerator;
import com.example.noteit.common.response.PageResponse;
import com.example.noteit.common.util.TimeProvider;
import com.example.noteit.relation.domain.RelationDomainService;
import com.example.noteit.relation.event.FollowRelationshipChangedEvent;
import com.example.noteit.relation.model.UserFollowDO;
import com.example.noteit.relation.model.UserFollowResponse;
import com.example.noteit.relation.repository.RelationRepository;
import com.example.noteit.user.model.UserProfileDO;
import com.example.noteit.user.model.UserProfileResponse;
import com.example.noteit.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RelationApplicationService {

    private static final int ACTIVE_STATUS = 1;
    private static final int INACTIVE_STATUS = 0;

    private final IdGenerator idGenerator;
    private final RelationDomainService relationDomainService;
    private final RelationRepository relationRepository;
    private final UserProfileRepository userProfileRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final TimeProvider timeProvider;

    public RelationApplicationService(
            IdGenerator idGenerator,
            RelationDomainService relationDomainService,
            RelationRepository relationRepository,
            UserProfileRepository userProfileRepository,
            DomainEventPublisher domainEventPublisher,
            TimeProvider timeProvider
    ) {
        this.idGenerator = idGenerator;
        this.relationDomainService = relationDomainService;
        this.relationRepository = relationRepository;
        this.userProfileRepository = userProfileRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public UserFollowResponse followUser(Long currentUserId, String targetUserId) {
        long followeeId = parseUserId(targetUserId);
        relationDomainService.validateCanFollow(currentUserId, followeeId);
        ensureActiveUser(currentUserId);
        ensureActiveUser(followeeId);

        UserFollowDO existing = relationRepository.findFollowRelation(currentUserId, followeeId).orElse(null);
        boolean changed = false;
        if (existing == null) {
            relationRepository.insertFollowRelation(new UserFollowDO(
                    idGenerator.nextId(),
                    currentUserId,
                    followeeId,
                    ACTIVE_STATUS,
                    null,
                    null
            ));
            incrementFollowCounters(currentUserId, followeeId, 1);
            changed = true;
        } else if (existing.status() != ACTIVE_STATUS) {
            relationRepository.updateFollowStatus(existing.id(), ACTIVE_STATUS);
            incrementFollowCounters(currentUserId, followeeId, 1);
            changed = true;
        }
        if (changed) {
            publishFollowChanged(currentUserId, followeeId, true);
        }

        return new UserFollowResponse(targetUserId, true);
    }

    @Transactional
    public UserFollowResponse unfollowUser(Long currentUserId, String targetUserId) {
        long followeeId = parseUserId(targetUserId);
        relationDomainService.validateCanFollow(currentUserId, followeeId);
        ensureActiveUser(currentUserId);
        ensureActiveUser(followeeId);

        UserFollowDO existing = relationRepository.findFollowRelation(currentUserId, followeeId)
                .filter(relation -> relation.status() != INACTIVE_STATUS)
                .orElse(null);
        boolean changed = existing != null;
        if (changed) {
            relationRepository.updateFollowStatus(existing.id(), INACTIVE_STATUS);
            incrementFollowCounters(currentUserId, followeeId, -1);
        }
        if (changed) {
            publishFollowChanged(currentUserId, followeeId, false);
        }

        return new UserFollowResponse(targetUserId, false);
    }

    public PageResponse<UserProfileResponse> getFollowingUsers(
            String userId,
            Long currentUserId,
            int pageNo,
            int pageSize
    ) {
        long parsedUserId = parseUserId(userId);
        ensureActiveUser(parsedUserId);

        int offset = (pageNo - 1) * pageSize;
        List<UserProfileResponse> records = relationRepository.findFollowingProfiles(parsedUserId, offset, pageSize)
                .stream()
                .map(profile -> toProfileResponse(profile, currentUserId))
                .toList();
        long total = relationRepository.countFollowingProfiles(parsedUserId);
        return new PageResponse<>(pageNo, pageSize, total, records);
    }

    public PageResponse<UserProfileResponse> getFollowerUsers(
            String userId,
            Long currentUserId,
            int pageNo,
            int pageSize
    ) {
        long parsedUserId = parseUserId(userId);
        ensureActiveUser(parsedUserId);

        int offset = (pageNo - 1) * pageSize;
        List<UserProfileResponse> records = relationRepository.findFollowerProfiles(parsedUserId, offset, pageSize)
                .stream()
                .map(profile -> toProfileResponse(profile, currentUserId))
                .toList();
        long total = relationRepository.countFollowerProfiles(parsedUserId);
        return new PageResponse<>(pageNo, pageSize, total, records);
    }

    private void incrementFollowCounters(long followerId, long followeeId, long delta) {
        userProfileRepository.incrementFollowingCount(followerId, delta);
        userProfileRepository.incrementFollowerCount(followeeId, delta);
    }

    private void publishFollowChanged(long followerUserId, long followeeUserId, boolean following) {
        domainEventPublisher.publish(new FollowRelationshipChangedEvent(
                followerUserId,
                followeeUserId,
                following,
                timeProvider.now()
        ));
    }

    private UserProfileDO ensureActiveUser(long userId) {
        return userProfileRepository.findById(userId)
                .filter(user -> user.status() == ACTIVE_STATUS)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "User profile was not found"
                ));
    }

    private UserProfileResponse toProfileResponse(UserProfileDO profile, Long currentUserId) {
        boolean followed = currentUserId != null
                && currentUserId != profile.id()
                && relationRepository.hasActiveFollow(currentUserId, profile.id());
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

    private long parseUserId(String rawUserId) {
        try {
            return Long.parseLong(rawUserId);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "userId must be a number");
        }
    }
}
