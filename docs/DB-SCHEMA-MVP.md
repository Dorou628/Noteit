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

## 6. Feed 读模型方向
当前不新增 Feed 专用收件箱 / 发件箱表到运行链路中，但文档层明确保留未来方向：

- 用户收件箱时间线
- 作者发件箱时间线
- 深分页游标字段
- 缓存分层后的读模型

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
