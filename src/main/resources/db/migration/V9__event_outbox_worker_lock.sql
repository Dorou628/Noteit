ALTER TABLE `event_outbox`
    MODIFY COLUMN `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0-new 1-sent 2-failed 3-processing 4-dead',
    ADD COLUMN `locked_by` VARCHAR(128) NULL COMMENT 'Worker instance that claimed the event' AFTER `next_retry_at`,
    ADD COLUMN `locked_until` DATETIME(3) NULL COMMENT 'Claim timeout for PROCESSING events' AFTER `locked_by`;

ALTER TABLE `event_outbox`
    DROP INDEX `idx_status_next_retry_at`,
    ADD KEY `idx_event_outbox_claim` (`status`, `next_retry_at`, `locked_until`, `created_at`, `id`);
