package com.example.noteit.interaction.model;

public record ArticleFavoriteResponse(
        String articleId,
        boolean favorited,
        long favoriteCount
) {
}
