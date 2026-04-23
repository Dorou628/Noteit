-- MVP demo data for frontend/backend integration.
-- The fixed IDs make it easy to open article detail pages directly during debugging.

INSERT INTO `user_profile` (
    `id`, `nickname`, `avatar_url`, `bio`, `follower_count`, `following_count`,
    `article_count`, `status`, `version`
) VALUES
    (
        9001001,
        'Noteit_穿搭研究所',
        'https://picsum.photos/seed/noteit-avatar-style/240/240',
        '记录通勤、周末和旅行里的真实穿搭。',
        128,
        12,
        2,
        1,
        0
    ),
    (
        9001002,
        'Noteit_城市漫游',
        'https://picsum.photos/seed/noteit-avatar-city/240/240',
        '把周末交给城市、咖啡和小路。',
        86,
        25,
        2,
        1,
        0
    ),
    (
        9001003,
        'Noteit_效率手帐',
        'https://picsum.photos/seed/noteit-avatar-notes/240/240',
        '用简单系统整理工作和生活。',
        64,
        18,
        2,
        1,
        0
    )
ON DUPLICATE KEY UPDATE
    `nickname` = VALUES(`nickname`),
    `avatar_url` = VALUES(`avatar_url`),
    `bio` = VALUES(`bio`),
    `follower_count` = VALUES(`follower_count`),
    `following_count` = VALUES(`following_count`),
    `article_count` = VALUES(`article_count`),
    `status` = VALUES(`status`),
    `updated_at` = CURRENT_TIMESTAMP(3);

INSERT INTO `article` (
    `id`, `author_id`, `title`, `content_text`, `content_storage_type`,
    `content_object_key`, `content_url`, `content_format`, `content_preview`,
    `cover_object_key`, `cover_url`, `status`, `like_count`, `favorite_count`,
    `summary_status`, `summary_text`, `summary_version`, `summary_retry_count`,
    `summary_last_error`, `published_at`, `version`
) VALUES
    (
        9101001,
        9001001,
        '春季通勤穿搭：三件单品撑起一周造型',
        '# 春季通勤穿搭\n\n这周的核心思路是把浅色风衣、直筒牛仔裤和乐福鞋固定下来，再用内搭颜色做变化。\n\n- 周一：白衬衫搭风衣，干净利落\n- 周三：针织短袖叠穿，适合温差大的天气\n- 周五：条纹上衣增加一点松弛感\n\n如果早上赶时间，可以提前把外套、裤子和鞋固定好，只换内搭和包。',
        'DB',
        NULL,
        NULL,
        'MARKDOWN',
        '浅色风衣、直筒牛仔裤和乐福鞋组成一周通勤公式，只换内搭就能有变化。',
        'dev-seed/article/image/9101001-cover.jpg',
        'https://picsum.photos/seed/noteit-article-9101001-cover/1200/900',
        1,
        2,
        1,
        2,
        '一套适合春季通勤的基础搭配公式，强调固定外套、裤子和鞋，再通过内搭变化提升效率。',
        1,
        0,
        NULL,
        CURRENT_TIMESTAMP(3) - INTERVAL 6 DAY,
        0
    ),
    (
        9101002,
        9001002,
        '周末城市漫游路线：从旧书店走到河边',
        '# 周末城市漫游路线\n\n这条路线适合不想安排太满的周末：先去旧书店翻一会儿画册，再沿着梧桐路走到河边。\n\n- 上午：旧书店和独立咖啡店\n- 下午：河边散步，顺路拍几张街景\n- 傍晚：找一家小馆子吃热汤面\n\n建议穿舒服的鞋，包里放一瓶水和轻便相机。',
        'DB',
        NULL,
        NULL,
        'MARKDOWN',
        '一条适合轻松周末的城市散步路线：旧书店、咖啡、河边和热汤面。',
        'dev-seed/article/image/9101002-cover.jpg',
        'https://picsum.photos/seed/noteit-article-9101002-cover/1200/900',
        1,
        1,
        2,
        2,
        '旧书店、咖啡店和河边散步串成一条轻量城市路线，适合不想赶行程的周末。',
        1,
        0,
        NULL,
        CURRENT_TIMESTAMP(3) - INTERVAL 5 DAY,
        0
    ),
    (
        9101003,
        9001003,
        '我的晨间 20 分钟整理法',
        '# 晨间 20 分钟整理法\n\n早上不适合做复杂计划，我会把整理拆成三个很小的动作。\n\n- 5 分钟：清空桌面，只留下今天要用的东西\n- 10 分钟：写下三件最重要的任务\n- 5 分钟：检查日程和消息，决定第一件事从哪里开始\n\n关键不是做得多，而是让一天有一个清晰入口。',
        'DB',
        NULL,
        NULL,
        'MARKDOWN',
        '用 20 分钟完成桌面、任务和日程整理，让一天从一个清晰入口开始。',
        'dev-seed/article/image/9101003-cover.jpg',
        'https://picsum.photos/seed/noteit-article-9101003-cover/1200/900',
        1,
        3,
        2,
        0,
        NULL,
        0,
        0,
        NULL,
        CURRENT_TIMESTAMP(3) - INTERVAL 4 DAY,
        0
    ),
    (
        9101004,
        9001001,
        '小个子也能穿长裙：比例比长度更重要',
        '# 小个子长裙搭配\n\n长裙不是小个子的禁区，重点是把视觉重心往上提。\n\n- 选择高腰线，让腿部比例更明显\n- 上衣尽量短一点，或者塞进裙腰\n- 鞋子颜色贴近下装，减少截断感\n\n如果裙摆比较宽，上半身就保持简洁。',
        'DB',
        NULL,
        NULL,
        'MARKDOWN',
        '小个子穿长裙的重点不是避开长度，而是用高腰线和简洁上装调整比例。',
        'dev-seed/article/image/9101004-cover.jpg',
        'https://picsum.photos/seed/noteit-article-9101004-cover/1200/900',
        1,
        1,
        0,
        2,
        '小个子穿长裙时应优先处理比例，通过高腰线、短上衣和连贯鞋色减少压个子的问题。',
        1,
        0,
        NULL,
        CURRENT_TIMESTAMP(3) - INTERVAL 3 DAY,
        0
    ),
    (
        9101005,
        9001002,
        '一家适合独处的咖啡店：窗边座位很加分',
        '# 适合独处的咖啡店\n\n这家店最舒服的位置是靠窗的小桌，下午三点会有一段很柔和的光。\n\n- 点单建议：热拿铁和柠檬磅蛋糕\n- 适合场景：看书、写计划、简单修图\n- 不太适合：多人聊天，店内座位不多\n\n如果只是想安静待一小时，这里刚刚好。',
        'DB',
        NULL,
        NULL,
        'MARKDOWN',
        '一间适合独处的咖啡店，窗边座位、热拿铁和安静氛围是重点。',
        'dev-seed/article/image/9101005-cover.jpg',
        'https://picsum.photos/seed/noteit-article-9101005-cover/1200/900',
        1,
        2,
        2,
        1,
        NULL,
        0,
        0,
        NULL,
        CURRENT_TIMESTAMP(3) - INTERVAL 2 DAY,
        0
    ),
    (
        9101006,
        9001003,
        '一页纸复盘：把项目进展说清楚',
        '# 一页纸复盘\n\n项目复盘不一定要写很长，我会用一页纸回答四个问题。\n\n- 目标是什么：一句话写清楚\n- 已完成什么：只列可验证结果\n- 卡在哪里：写事实，不写情绪\n- 下一步是什么：明确负责人和时间\n\n这样复盘会更适合周会和跨团队同步。',
        'DB',
        NULL,
        NULL,
        'MARKDOWN',
        '用一页纸讲清楚目标、完成项、阻塞点和下一步，适合项目周会同步。',
        'dev-seed/article/image/9101006-cover.jpg',
        'https://picsum.photos/seed/noteit-article-9101006-cover/1200/900',
        1,
        0,
        1,
        2,
        '一页纸复盘将项目同步压缩到目标、结果、阻塞和下一步四个问题，便于周会沟通。',
        1,
        0,
        NULL,
        CURRENT_TIMESTAMP(3) - INTERVAL 1 DAY,
        0
    )
ON DUPLICATE KEY UPDATE
    `author_id` = VALUES(`author_id`),
    `title` = VALUES(`title`),
    `content_text` = VALUES(`content_text`),
    `content_storage_type` = VALUES(`content_storage_type`),
    `content_object_key` = VALUES(`content_object_key`),
    `content_url` = VALUES(`content_url`),
    `content_format` = VALUES(`content_format`),
    `content_preview` = VALUES(`content_preview`),
    `cover_object_key` = VALUES(`cover_object_key`),
    `cover_url` = VALUES(`cover_url`),
    `status` = VALUES(`status`),
    `like_count` = VALUES(`like_count`),
    `favorite_count` = VALUES(`favorite_count`),
    `summary_status` = VALUES(`summary_status`),
    `summary_text` = VALUES(`summary_text`),
    `summary_version` = VALUES(`summary_version`),
    `summary_retry_count` = VALUES(`summary_retry_count`),
    `summary_last_error` = VALUES(`summary_last_error`),
    `published_at` = VALUES(`published_at`),
    `updated_at` = CURRENT_TIMESTAMP(3);

INSERT INTO `article_media` (
    `id`, `article_id`, `media_type`, `object_key`, `media_url`, `sort_no`, `is_cover`
) VALUES
    (9201001, 9101001, 'IMAGE', 'dev-seed/article/image/9101001-1.jpg', 'https://picsum.photos/seed/noteit-article-9101001-1/1200/900', 1, 1),
    (9201002, 9101001, 'IMAGE', 'dev-seed/article/image/9101001-2.jpg', 'https://picsum.photos/seed/noteit-article-9101001-2/1200/900', 2, 0),
    (9201003, 9101001, 'IMAGE', 'dev-seed/article/image/9101001-3.jpg', 'https://picsum.photos/seed/noteit-article-9101001-3/1200/900', 3, 0),
    (9201004, 9101002, 'IMAGE', 'dev-seed/article/image/9101002-1.jpg', 'https://picsum.photos/seed/noteit-article-9101002-1/1200/900', 1, 1),
    (9201005, 9101002, 'IMAGE', 'dev-seed/article/image/9101002-2.jpg', 'https://picsum.photos/seed/noteit-article-9101002-2/1200/900', 2, 0),
    (9201006, 9101003, 'IMAGE', 'dev-seed/article/image/9101003-1.jpg', 'https://picsum.photos/seed/noteit-article-9101003-1/1200/900', 1, 1),
    (9201007, 9101003, 'IMAGE', 'dev-seed/article/image/9101003-2.jpg', 'https://picsum.photos/seed/noteit-article-9101003-2/1200/900', 2, 0),
    (9201008, 9101004, 'IMAGE', 'dev-seed/article/image/9101004-1.jpg', 'https://picsum.photos/seed/noteit-article-9101004-1/1200/900', 1, 1),
    (9201009, 9101004, 'IMAGE', 'dev-seed/article/image/9101004-2.jpg', 'https://picsum.photos/seed/noteit-article-9101004-2/1200/900', 2, 0),
    (9201010, 9101005, 'IMAGE', 'dev-seed/article/image/9101005-1.jpg', 'https://picsum.photos/seed/noteit-article-9101005-1/1200/900', 1, 1),
    (9201011, 9101005, 'IMAGE', 'dev-seed/article/image/9101005-2.jpg', 'https://picsum.photos/seed/noteit-article-9101005-2/1200/900', 2, 0),
    (9201012, 9101006, 'IMAGE', 'dev-seed/article/image/9101006-1.jpg', 'https://picsum.photos/seed/noteit-article-9101006-1/1200/900', 1, 1),
    (9201013, 9101006, 'IMAGE', 'dev-seed/article/image/9101006-2.jpg', 'https://picsum.photos/seed/noteit-article-9101006-2/1200/900', 2, 0)
ON DUPLICATE KEY UPDATE
    `article_id` = VALUES(`article_id`),
    `media_type` = VALUES(`media_type`),
    `object_key` = VALUES(`object_key`),
    `media_url` = VALUES(`media_url`),
    `sort_no` = VALUES(`sort_no`),
    `is_cover` = VALUES(`is_cover`),
    `updated_at` = CURRENT_TIMESTAMP(3);

INSERT INTO `article_like` (
    `id`, `article_id`, `user_id`, `status`
) VALUES
    (9301001, 9101001, 9001002, 1),
    (9301002, 9101001, 9001003, 1),
    (9301003, 9101002, 9001001, 1),
    (9301004, 9101003, 9001001, 1),
    (9301005, 9101003, 9001002, 1),
    (9301006, 9101003, 9001003, 1),
    (9301007, 9101004, 9001002, 1),
    (9301008, 9101005, 9001001, 1),
    (9301009, 9101005, 9001003, 1)
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `updated_at` = CURRENT_TIMESTAMP(3);

INSERT INTO `article_favorite` (
    `id`, `article_id`, `user_id`, `status`
) VALUES
    (9401001, 9101001, 9001002, 1),
    (9401002, 9101002, 9001001, 1),
    (9401003, 9101002, 9001003, 1),
    (9401004, 9101003, 9001001, 1),
    (9401005, 9101003, 9001002, 1),
    (9401006, 9101005, 9001001, 1),
    (9401007, 9101005, 9001003, 1),
    (9401008, 9101006, 9001001, 1)
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `updated_at` = CURRENT_TIMESTAMP(3);
