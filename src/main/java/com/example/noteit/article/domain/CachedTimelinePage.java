package com.example.noteit.article.domain;

import java.util.List;

public record CachedTimelinePage(
        List<Long> articleIds,
        long total
) {
}
