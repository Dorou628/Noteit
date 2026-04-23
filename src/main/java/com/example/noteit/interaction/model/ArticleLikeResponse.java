package com.example.noteit.interaction.model;

public record ArticleLikeResponse(
        String articleId,
        boolean liked,
        long likeCount
) {
}
