package com.example.noteit.relation.model;

public record UserFollowResponse(
        String userId,
        boolean followed
) {
}
