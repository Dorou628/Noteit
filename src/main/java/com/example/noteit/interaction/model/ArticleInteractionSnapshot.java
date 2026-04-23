package com.example.noteit.interaction.model;

public record ArticleInteractionSnapshot(
        long likeCount,
        long favoriteCount,
        boolean liked,
        boolean favorited
) {
}
