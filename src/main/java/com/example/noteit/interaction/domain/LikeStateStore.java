package com.example.noteit.interaction.domain;

public interface LikeStateStore {

    boolean like(Long userId, Long articleId);

    boolean unlike(Long userId, Long articleId);

    boolean hasLiked(Long userId, Long articleId);
}
