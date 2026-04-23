package com.example.noteit.article.model;

public record AuthorView(
        String id,
        String nickname,
        String avatarUrl,
        boolean followed
) {
}
