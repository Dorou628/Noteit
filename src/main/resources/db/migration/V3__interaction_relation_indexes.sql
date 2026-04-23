-- 为文章详情页互动统计补充查询索引。
-- 当前仍然用关系表实时聚合计数，因此优先优化 article_id + status 的过滤。

ALTER TABLE `article_like`
    ADD INDEX `idx_article_id_status_created_at` (`article_id`, `status`, `created_at` DESC);

ALTER TABLE `article_favorite`
    ADD INDEX `idx_article_id_status_created_at` (`article_id`, `status`, `created_at` DESC);
