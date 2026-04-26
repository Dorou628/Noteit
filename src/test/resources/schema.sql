DROP TABLE IF EXISTS article_media;
DROP TABLE IF EXISTS article_like;
DROP TABLE IF EXISTS article_favorite;
DROP TABLE IF EXISTS event_outbox;
DROP TABLE IF EXISTS user_inbox;
DROP TABLE IF EXISTS article_outbox;
DROP TABLE IF EXISTS user_follow;
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS upload_task;
DROP TABLE IF EXISTS auth_user;
DROP TABLE IF EXISTS user_profile;

CREATE TABLE user_profile (
    id BIGINT PRIMARY KEY,
    nickname VARCHAR(64) NOT NULL,
    avatar_url VARCHAR(512),
    bio VARCHAR(255),
    follower_count BIGINT NOT NULL DEFAULT 0,
    following_count BIGINT NOT NULL DEFAULT 0,
    article_count BIGINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE article (
    id BIGINT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content_text CLOB,
    content_storage_type VARCHAR(16) NOT NULL DEFAULT 'DB',
    content_object_key VARCHAR(512),
    content_url VARCHAR(512),
    content_format VARCHAR(32) NOT NULL DEFAULT 'MARKDOWN',
    content_preview VARCHAR(500),
    cover_object_key VARCHAR(512),
    cover_url VARCHAR(512),
    status TINYINT NOT NULL DEFAULT 1,
    like_count BIGINT NOT NULL DEFAULT 0,
    favorite_count BIGINT NOT NULL DEFAULT 0,
    summary_status TINYINT NOT NULL DEFAULT 0,
    summary_text VARCHAR(1000),
    summary_version BIGINT NOT NULL DEFAULT 0,
    summary_retry_count INT NOT NULL DEFAULT 0,
    summary_last_error VARCHAR(512),
    published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE article_outbox (
    id BIGINT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    published_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_article_outbox_author_article UNIQUE (author_id, article_id)
);

CREATE TABLE user_inbox (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    published_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_inbox_user_article UNIQUE (user_id, article_id)
);

CREATE TABLE article_media (
    id BIGINT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    media_type VARCHAR(32) NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    media_url VARCHAR(512) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    is_cover TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE article_like (
    id BIGINT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_article_like_article_user UNIQUE (article_id, user_id)
);

CREATE TABLE article_favorite (
    id BIGINT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_article_favorite_article_user UNIQUE (article_id, user_id)
);

CREATE TABLE user_follow (
    id BIGINT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_follow_follower_followee UNIQUE (follower_id, followee_id)
);

CREATE TABLE event_outbox (
    id BIGINT PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    payload CLOB NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP,
    last_error VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_event_outbox_event_id UNIQUE (event_id)
);

CREATE TABLE upload_task (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    biz_type VARCHAR(32) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    content_length BIGINT NOT NULL DEFAULT 0,
    object_key VARCHAR(512) NOT NULL,
    public_url VARCHAR(512),
    status TINYINT NOT NULL DEFAULT 0,
    etag VARCHAR(128),
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth_user (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    password_plain_text VARCHAR(128) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_auth_user_user_id UNIQUE (user_id),
    CONSTRAINT uk_auth_user_username UNIQUE (username)
);
