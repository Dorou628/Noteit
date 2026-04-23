ALTER TABLE `article`
    ADD COLUMN `content_text` MEDIUMTEXT NULL COMMENT 'Article body stored in database for local debugging' AFTER `title`,
    ADD COLUMN `content_storage_type` VARCHAR(16) NOT NULL DEFAULT 'DB' COMMENT 'Article content storage type: DB or OSS' AFTER `content_text`;

ALTER TABLE `article`
    MODIFY COLUMN `content_object_key` VARCHAR(512) NULL COMMENT 'Body file OSS object key',
    MODIFY COLUMN `content_url` VARCHAR(512) NULL COMMENT 'Body file public URL';

UPDATE `article`
SET `content_storage_type` = CASE
    WHEN `content_text` IS NULL AND (`content_object_key` IS NOT NULL OR `content_url` IS NOT NULL) THEN 'OSS'
    ELSE 'DB'
END;
