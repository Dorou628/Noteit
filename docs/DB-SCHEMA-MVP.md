# Noteit MVP 数据库设计

## 1. 当前实现原则
- MySQL 只承载当前 MVP 主链路
- 正文当前直接存数据库
- 图片和头像只保存 OSS 引用
- 高频计数最终不以实体主表冗余字段为核心方案
- MVP 当前默认使用本地简单递增 ID，便于联调和测试断言
- 代码中保留 `SnowflakeIdGenerator`，后续切换雪花算法时替换 `IdGenerator` 实现即可

## 2. 当前核心表
- `user_profile`
- `auth_user`
- `article`
- `article_media`
- `article_like`
- `article_favorite`
- `user_follow`
- `upload_task`
- `event_outbox`

## 3. article 表当前约定
## 3. 登录与用户资料
### 3.1 user_profile
- `user_profile` 只保存用户展示资料，不保存密码
- 当前包括昵称、头像、简介、关注数、文章数等

### 3.2 auth_user
- `auth_user` 保存 MVP 登录账号
- `user_id` 关联 `user_profile.id`
- `username` 为登录用户名
- `password_plain_text` 为 MVP 调试阶段明文密码
- 后续正式化时必须迁移为 `password_hash`，并接入 JWT 双令牌

## 4. article 表当前约定
### 4.1 当前正文相关字段
- `content_text`：当前正文全文
- `content_storage_type`：当前为 `DB`
- `content_object_key`：未来 OSS 正文兼容字段
- `content_url`：未来 OSS 正文兼容字段
- `content_format`
- `content_preview`

### 4.2 当前计数字段
- `like_count`
- `favorite_count`

说明：
- 这两个字段当前视为兼容字段
- 新实现不要继续加深对这两个字段的强依赖
- 最终高频计数方案将迁移到独立状态存储和读模型

## 5. 互动与关系表
### 5.1 点赞
- `article_like`
- 当前 MVP 用关系表实现幂等
- 最终目标：分片 Bitmap

### 5.2 收藏
- `article_favorite`
- 当前 MVP 用关系表实现幂等
- 最终目标：ZSet

### 5.3 关注
- `user_follow`
- 当前 MVP 用关系表实现幂等
- 最终目标：Outbox + Canal + Kafka 驱动的一致性链路

## 6. Feed 读模型
当前已经新增 MySQL 版收件箱 / 发件箱表，用于支撑关注 Feed 的写扩散读模型：

- `article_outbox`：作者发件箱时间线，一条记录表示某作者发布了一篇文章。
- `user_inbox`：用户收件箱时间线，一条记录表示某用户收到了一篇关注作者的文章。
- 发布文章时，业务事务写 `event_outbox`；后台 worker 消费 `ArticlePublished` 后写入 `article_outbox`，并投递到粉丝 `user_inbox`。
- 关注作者时，业务事务写 `event_outbox`；后台 worker 消费 `FollowRelationshipChanged` 后从 `article_outbox` 回填该作者最近文章到当前用户 `user_inbox`。
- 取关作者时，业务事务写 `event_outbox`；后台 worker 消费后删除当前用户收件箱中该作者的文章记录。
- 当前仍使用 `pageNo/pageSize`；后续深分页可引入游标字段。
- Redis ZSet 已作为缓存层接入，MySQL 仍是事实数据源。

### 6.1 DB outbox 阶段一
当前先用数据库 outbox + 本地 worker 实现异步化，还没有引入 Canal / Kafka：

| 事件 | 生产时机 | worker 动作 |
| --- | --- | --- |
| `ArticlePublished` | 创建文章事务内写入 `event_outbox` | 写作者 `article_outbox`，向当前粉丝写 `user_inbox`，维护已存在的 Redis outbox/inbox |
| `ArticleDeleted` | 删除文章事务内写入 `event_outbox` | 删除 MySQL outbox/inbox 中该文章，失效作者 outbox 和粉丝 inbox 缓存 |
| `FollowRelationshipChanged` | 关注/取关事务内写入 `event_outbox` | 关注时回填当前用户 inbox；取关时清理当前用户 inbox 并失效缓存 |
| `ArticleUpdated` | 更新文章事务内写入 `event_outbox` | 当前不改时间线；文章详情回表读取，所以无需处理 feed 缓存 |

worker 处理规则：

- 轮询 `event_outbox.status IN (0, 2)` 且到达 `next_retry_at` 的事件。
- 处理成功后标记 `status=1`。
- 处理失败后标记 `status=2`，增加 `retry_count`，设置下一次重试时间。
- 派生模型写入必须保持幂等，例如 outbox/inbox 通过唯一键避免重复投递。

### 6.2 Redis ZSet 映射
当前 Redis 只缓存时间线 ID，不缓存文章完整内容：

| 语义 | Redis key | 类型 | member | score |
| --- | --- | --- | --- | --- |
| 用户收件箱 | `noteit:feed:inbox:{userId}` | ZSet | `articleId` | `publishedAtMillis + articleId tie-breaker` |
| 作者发件箱 | `noteit:feed:outbox:{authorId}` | ZSet | `articleId` | `publishedAtMillis + articleId tie-breaker` |
| 收件箱总数 | `noteit:feed:inbox:{userId}:total` | String | 计数值 | 不适用 |
| 发件箱总数 | `noteit:feed:outbox:{authorId}:total` | String | 计数值 | 不适用 |

缓存策略：

- `GET /api/v1/users/me/feed` 优先读收件箱 ZSet，然后按 ID 批量回表查询文章卡片详情。
- Redis key 不存在时视为缓存未命中，走 MySQL `user_inbox` 查询，并回填最近 `noteit.feed.cache.rebuild-limit` 条收件箱记录。
- 如果请求页超出已回填的 ZSet 窗口，但 MySQL 总数仍更多，会退回 MySQL，避免深分页返回不完整数据。
- 关注作者时优先读作者发件箱 ZSet；发件箱 key 不存在时，使用 MySQL `article_outbox` 最近窗口重建 Redis，再回填当前用户 `user_inbox`。
- 发件箱 ZSet 同样是最近窗口缓存，不代表作者全量历史文章。
- 发布文章和关注回填只在对应 key 已存在时增量写 Redis，避免创建只有局部数据的不完整缓存。
- 取关后直接删除当前用户收件箱缓存，由下一次读取从 MySQL 重建。
- 删除文章时软删除 `article`，同步删除 MySQL outbox/inbox 时间线记录，并失效作者 outbox 与当前粉丝 inbox 缓存，缓存由后续读取重建。
- Redis 异常不影响业务结果，读接口会降级到 MySQL。
- 默认测试环境关闭缓存；本地环境可通过 `noteit.feed.cache.enabled=true` 开启。

## 7. 上传任务
`upload_task` 当前继续保留：
- `ARTICLE_IMAGE`
- `AVATAR`
- `ARTICLE_CONTENT` 兼容类型

说明：
- 虽然正文当前不依赖 OSS 上传，但保留 `ARTICLE_CONTENT` 可以避免未来重新设计上传模块

## 8. 迁移原则
- 所有结构调整通过 Flyway 新增版本完成
- 不回写已有迁移
- 当前正文数据库存储已经通过 `V2` 迁移落地
