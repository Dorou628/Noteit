# Noteit MVP 交付计划

## 1. 交付目标
当前交付目标只有一个：先把可运行的 MVP 做稳定，同时为最终架构留下明确边界，不提前引入复杂中间件。

接口契约以 `docs/API-MVP.md` 为准。每个阶段推进前，必须先确认对应接口的请求、成功响应、失败响应、分页结构和当前状态标记已经写清楚；实现完成后再把接口状态从“阶段占位”更新为“已实现”。

## 2. 当前开发顺序
### 阶段 1：文档与抽象边界
- 对齐 PRD / API / DB / 架构 / 交付计划
- 明确当前实现与最终目标的分层
- 增加认证、Feed、互动、关注的抽象接口

### 阶段 2：认证边界稳定化
- 提供 MVP 明文密码登录接口
- 登录成功后仍保持请求头模拟鉴权
- 统一通过 `CurrentUserResolver` 读取当前用户
- 仅预留 JWT 双令牌接口和服务边界

### 阶段 3：互动 MVP
- MySQL 跑通点赞 / 取消点赞
- MySQL 跑通收藏 / 取消收藏
- 跑通“我赞过的 / 我收藏的”
- 验证幂等
- 对应 API 文档：`PUT/DELETE /articles/{articleId}/like`、`PUT/DELETE /articles/{articleId}/favorite`、`GET /users/me/liked-articles`、`GET /users/me/favorited-articles`

### 阶段 4：关注 MVP
- MySQL 跑通关注 / 取关（已完成）
- 关注列表 / 粉丝列表（已完成）
- 用户主页基础资料已前置完成；真实关注状态已补齐
- 文章作者视图中的 `followed` 已补齐
- 保留后续事件链路扩展点
- 对应 API 文档：`PUT/DELETE /users/{userId}/follow`、`GET /users/{userId}`、文章作者视图中的 `followed`

### 阶段 5：Feed MVP
- 跑通首页 Feed
- 跑通个人已发布列表
- 编辑个人信息（已完成）
- 跑通分页与基础排序
- 文档中说明后续收件箱 / 发件箱与深分页演进
- 对应 API 文档：`GET /articles`、`GET /users/{userId}/articles`

### 阶段 6：认证正式化
- 最后再接 JWT 双令牌
- 不影响前面业务模块的边界
- 对应 API 文档：登录响应增加 token、`POST /auth/refresh`、`POST /auth/logout`

## 3. TDD 执行规则
- 先写失败测试
- 再写最小实现
- 测试通过后再重构
- 每次只推进一个业务闭环

## 4. 当前验收口径
- 文档与代码一致
- 每个接口都有明确请求格式、成功响应格式和主要失败响应格式
- 现有文章正文数据库存储不回归
- 图片/头像直传 OSS 不回归
- 新增抽象不影响现有测试通过
- 后续互动、关注、Feed 开发时不需要再先重写文档基线
