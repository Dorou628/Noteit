# Noteit MVP 技术架构

## 1. 文档目标
本文档描述两层内容：

- `MVP 当前实现`：现在真正要落地和运行的方案
- `最终目标架构`：已经明确的高并发/高一致性演进方向

## 2. MVP 当前实现
### 2.1 技术栈
- Java 17
- Spring Boot
- MyBatis
- MySQL 8
- Flyway
- 阿里云 OSS（仅图片/头像直传）

### 2.2 架构形态
- 模块化单体
- 统一 REST API
- MySQL 负责核心业务数据
- OSS 负责图片和头像原始文件
- 正文当前直接存数据库

### 2.3 模块划分
- `common`：统一响应、异常、上下文、事件、认证抽象
- `article`：文章发布、更新、详情、Feed 抽象
- `interaction`：点赞、收藏、沉淀列表
- `relation`：关注关系
- `user`：个人主页、个人文章
- `file`：上传任务、OSS 直传
- `summary`：摘要状态与后续 AI 接入

### 2.4 认证边界
- 当前提供 MVP 明文密码登录接口，用于前后端联调
- 当前业务接口仍使用请求头登录态：`X-User-Id`、`X-User-Nickname`
- 控制器统一通过 `CurrentUserResolver` 获取当前用户
- 登录账号存储在 `auth_user`，用户展示资料存储在 `user_profile`
- 预留：
  - `AuthFacade`
  - `TokenService`
  - JWT 双令牌接口

### 2.5 ID 生成边界
- 当前 MVP 默认使用 `LocalIdGenerator` 的本地简单递增 ID，优先方便联调和自动化测试断言
- 预留 `SnowflakeIdGenerator`，后续需要分布式部署或高并发写入时再切换
- 业务代码统一依赖 `IdGenerator` 接口，不直接感知具体 ID 算法

### 2.6 Feed 边界
- 当前 MVP 可以先用 MySQL 直接查询
- 预留：
  - `FeedReadService`
  - `FeedFanoutGateway`
  - `UserTimelineRepository`

### 2.7 互动与关系边界
- 当前 MVP：MySQL 直接实现点赞、收藏、关注
- 预留：
  - `LikeStateStore`
  - `FavoriteStateStore`
  - `FollowRelationEventPublisher`

## 3. 最终目标架构
### 3.1 Feed
- 收件箱 / 发件箱推拉结合
- 读路径采用三级缓存
- 深分页逐步迁移到游标化读模型

### 3.2 登录
- JWT 双令牌
- Access Token 负责接口鉴权
- Refresh Token 负责续期

### 3.3 点赞 / 收藏 / 关注
- 点赞：分片 Bitmap
- 收藏：ZSet
- 关注一致性：Outbox + Canal + Kafka

### 3.4 一致性
- 当前主链路优先单库事务
- 后续通过事件链路和读模型提升性能与扩展性

## 4. 设计原则
- 当前实现必须可测试、可运行、可联调
- 未来演进必须能通过替换实现完成，而不是重写业务边界
- 高频计数最终不依赖文章主表冗余字段
- 复杂缓存和异步链路只在真正需要时再接入

## 5. 开发原则
- 严格遵循 TDD
- 每次只推进一个完整模块闭环
- 先文档与抽象，后实现与测试，最后回归
