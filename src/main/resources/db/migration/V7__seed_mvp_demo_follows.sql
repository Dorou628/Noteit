-- Seed MVP demo follow relations for frontend/backend integration tests.

INSERT INTO `user_follow` (
    `id`, `follower_id`, `followee_id`, `status`
) VALUES
    (5001, 101, 102, 1),
    (5002, 101, 103, 1),
    (5003, 102, 101, 1),
    (5004, 103, 101, 1),
    (5005, 103, 102, 1)
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `updated_at` = CURRENT_TIMESTAMP(3);

UPDATE `user_profile`
SET `following_count` = CASE `id`
        WHEN 101 THEN 2
        WHEN 102 THEN 1
        WHEN 103 THEN 2
        ELSE `following_count`
    END,
    `follower_count` = CASE `id`
        WHEN 101 THEN 2
        WHEN 102 THEN 2
        WHEN 103 THEN 1
        ELSE `follower_count`
    END,
    `updated_at` = CURRENT_TIMESTAMP(3)
WHERE `id` IN (101, 102, 103);
