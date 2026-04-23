package com.example.noteit.interaction.domain;

public interface FavoriteStateStore {

    boolean favorite(Long userId, Long articleId);

    boolean unfavorite(Long userId, Long articleId);

    boolean hasFavorited(Long userId, Long articleId);
}
