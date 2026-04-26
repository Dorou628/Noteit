CREATE TABLE IF NOT EXISTS `article_outbox` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT 'Timeline row ID',
    `author_id` BIGINT UNSIGNED NOT NULL COMMENT 'Author user ID',
    `article_id` BIGINT UNSIGNED NOT NULL COMMENT 'Article ID',
    `published_at` DATETIME NOT NULL COMMENT 'Article publish time used for timeline ordering',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_outbox_author_article` (`author_id`, `article_id`),
    KEY `idx_article_outbox_author_time` (`author_id`, `published_at`, `article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Author outbox timeline';

CREATE TABLE IF NOT EXISTS `user_inbox` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT 'Timeline row ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'Receiver user ID',
    `author_id` BIGINT UNSIGNED NOT NULL COMMENT 'Author user ID',
    `article_id` BIGINT UNSIGNED NOT NULL COMMENT 'Article ID',
    `published_at` DATETIME NOT NULL COMMENT 'Article publish time used for timeline ordering',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_inbox_user_article` (`user_id`, `article_id`),
    KEY `idx_user_inbox_user_time` (`user_id`, `published_at`, `article_id`),
    KEY `idx_user_inbox_user_author` (`user_id`, `author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User inbox timeline';
