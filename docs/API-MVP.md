# Noteit MVP API 文档

## 1. 文档边界
本文档是 Noteit MVP 阶段的接口契约，必须同时约束两件事：

- 当前已经实现的接口，返回格式必须与本文档一致。
- 后续按交付计划推进的接口，必须先保持本文档中的外部契约稳定，再替换内部实现。

接口状态说明：

| 状态 | 含义 |
| --- | --- |
| 已实现 | 当前代码已经有真实业务实现和自动化测试保护 |
| 阶段占位 | 当前 Controller/Service 已有接口形态，但数据仍为空页、TODO 示例或最小返回，后续阶段补齐真实查询 |
| 后续预留 | 当前不开放运行链路，只作为未来设计边界 |

## 2. 通用约定
### 2.1 Base Path
- 所有业务接口统一以 `/api/v1` 开头。
- ID 对外统一使用字符串，服务端内部可以转换为 Long。
- 时间字段统一返回 ISO-8601 字符串，例如 `2026-04-18T16:24:07+08:00`。

### 2.2 请求头
| Header | 必填 | 说明 |
| --- | --- | --- |
| `X-Request-Id` | 否 | 调用方传入时原样回传；不传时后端生成 `req-{uuid}` |
| `X-User-Id` | 需登录接口必填 | 当前 MVP 使用的模拟登录用户 ID，必须是数字字符串 |
| `X-User-Nickname` | 否 | 当前 MVP 使用的模拟登录昵称，创建作者资料时可作为昵称来源 |

需登录接口：
- 创建/更新文章
- 创建/确认上传任务
- 点赞/取消点赞
- 收藏/取消收藏
- 关注/取关
- 查询“我赞过的”“我收藏的”

登录可选接口：
- 文章详情：未登录时 `liked=false`、`favorited=false`
- 首页 Feed、用户主页、用户已发布文章列表：当前接口形态允许无登录；传入 `X-User-Id` 时返回真实 `followed` 当前用户态

### 2.3 统一成功响应
所有成功响应外层固定为：

```json
{
  "code": "0",
  "message": "success",
  "requestId": "req-xxx",
  "data": {}
}
```

### 2.4 统一失败响应
所有失败响应外层固定为：

```json
{
  "code": "ARTICLE_NOT_FOUND",
  "message": "Article was not found",
  "requestId": "req-xxx"
}
```

说明：
- 失败时 `data` 为 `null`，当前全局 JSON 配置会省略该字段。
- `message` 默认使用错误码内置文案；参数校验异常可能返回框架生成的详细校验信息。

### 2.5 分页响应
分页接口的 `data` 固定为：

```json
{
  "pageNo": 1,
  "pageSize": 10,
  "total": 0,
  "records": []
}
```

分页参数：

| 参数 | 默认值 | 约束 |
| --- | --- | --- |
| `pageNo` | `1` | 最小 `1` |
| `pageSize` | `10` | 最小 `1`，最大 `20` |

## 3. 通用数据结构
### 3.1 ArticleDetail
用于创建文章、更新文章、获取文章详情。

```json
{
  "id": "10001",
  "title": "春季通勤穿搭",
  "content": "# 标题\n正文内容",
  "contentObjectKey": null,
  "contentUrl": null,
  "contentFormat": "MARKDOWN",
  "contentPreview": "春季通勤穿搭预览",
  "coverUrl": "https://cdn.example.com/article/image/cover.jpg",
  "imageUrls": [
    "https://cdn.example.com/article/image/1.jpg"
  ],
  "author": {
    "id": "3001",
    "nickname": "Tester",
    "avatarUrl": null,
    "followed": false
  },
  "summary": {
    "status": "PENDING",
    "content": null,
    "updatedAt": "2026-04-18T16:24:07+08:00"
  },
  "likeCount": 0,
  "favoriteCount": 0,
  "liked": false,
  "favorited": false,
  "createdAt": "2026-04-18T16:24:07+08:00",
  "updatedAt": "2026-04-18T16:24:07+08:00"
}
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| `content` | 当前 MVP 正文直接存数据库并在详情返回 |
| `contentObjectKey` / `contentUrl` | 当前 DB 正文模式下返回 `null`；为未来正文切 OSS 保留 |
| `imageUrls` | 文章图片 URL 列表，仅保存 OSS 引用 |
| `summary.status` | 可选值：`PENDING`、`PROCESSING`、`SUCCEEDED`、`FAILED` |
| `likeCount` / `favoriteCount` | 当前详情页以互动关系表聚合结果为准 |
| `liked` / `favorited` | 当前用户的互动状态；未登录时均为 `false` |

### 3.2 ArticleCard
用于 Feed、用户已发布文章、我赞过的文章、我收藏的文章。

```json
{
  "id": "10001",
  "title": "春季通勤穿搭",
  "contentPreview": "春季通勤穿搭预览",
  "coverUrl": "https://cdn.example.com/article/image/cover.jpg",
  "author": {
    "id": "3001",
    "nickname": "Tester",
    "avatarUrl": null,
    "followed": false
  },
  "summary": {
    "status": "PENDING",
    "content": null,
    "updatedAt": "2026-04-18T16:24:07+08:00"
  },
  "likeCount": 0,
  "favoriteCount": 0,
  "liked": false,
  "favorited": false,
  "createdAt": "2026-04-18T16:24:07+08:00",
  "updatedAt": "2026-04-18T16:24:07+08:00"
}
```

说明：
- 列表卡片不返回 `content` 正文全文，只返回 `contentPreview`。
- 当前阶段部分列表接口仍是阶段占位，真实数据在后续阶段补齐，但返回结构必须沿用该结构。

### 3.3 UploadTask
用于上传任务创建和确认。

```json
{
  "uploadTaskId": "20001",
  "objectKey": "article/image/2026/04/18/20001-cover.jpg",
  "uploadMethod": "POST",
  "uploadUrl": "https://noteit-test.oss-cn-test.aliyuncs.com",
  "headers": {},
  "formFields": {
    "key": "article/image/2026/04/18/20001-cover.jpg",
    "policy": "base64-policy",
    "Signature": "signature"
  },
  "expiredAt": "2026-04-18T16:39:07+08:00",
  "publicUrl": "https://cdn.example.com/article/image/cover.jpg",
  "status": "CREATED"
}
```

`status` 可选值：
- `CREATED`
- `UPLOADED`
- `CONFIRMED`
- `FAILED`
- `EXPIRED`

### 3.4 错误码与 HTTP 状态
| 错误码 | HTTP 状态 | 说明 |
| --- | --- | --- |
| `UNAUTHORIZED` | 401 | 需要登录但缺少 `X-User-Id` |
| `AUTH_BAD_CREDENTIALS` | 401 | 登录用户名或密码错误 |
| `FORBIDDEN` | 403 | 没有操作权限，例如确认他人的上传任务 |
| `ARTICLE_AUTHOR_MISMATCH` | 403 | 非作者更新文章 |
| `INVALID_PARAMETER` | 400 | 请求体、查询参数或 JSON 格式不合法 |
| `RESOURCE_NOT_FOUND` | 404 | 通用资源不存在 |
| `ARTICLE_NOT_FOUND` | 404 | 文章不存在或文章 ID 格式非法 |
| `UPLOAD_TASK_NOT_FOUND` | 404 | 上传任务不存在或上传任务 ID 格式非法 |
| `FOLLOW_SELF_NOT_ALLOWED` | 400 | 关注或取关自己 |
| `INTERNAL_ERROR` | 500 | 未预期服务端异常 |

## 4. 认证接口
### 4.1 当前鉴权方式
状态：已实现

当前业务接口仍通过请求头模拟登录态：

```http
X-User-Id: 3001
X-User-Nickname: Tester
```

说明：
- 前端登录成功后，需要把登录响应中的 `userId`、`nickname` 存起来。
- 后续调用创建文章、点赞、收藏、上传等接口时，由前端请求拦截器自动带上 `X-User-Id`、`X-User-Nickname`。
- 该方案只用于 MVP 联调，不具备防冒用能力。

### 4.2 登录
状态：已实现

```http
POST /api/v1/auth/login
Content-Type: application/json
```

请求体：

```json
{
  "username": "style",
  "password": "123456"
}
```

请求字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `username` | 是 | 登录用户名 |
| `password` | 是 | MVP 调试阶段明文密码 |

成功响应：

```json
{
  "code": "0",
  "message": "success",
  "requestId": "req-xxx",
  "data": {
    "userId": "101",
    "nickname": "Noteit_穿搭研究所",
    "avatarUrl": "https://picsum.photos/seed/noteit-avatar-style/240/240",
    "authMode": "HEADER_MOCK"
  }
}
```

说明：
- 当前 `authMode=HEADER_MOCK` 表示登录后仍由前端带用户请求头完成业务接口联调。
- 当前不返回 `accessToken` / `refreshToken`，这两个字段已在响应模型中预留，因值为 `null` 会被全局 JSON 配置省略。
- 当前数据库字段为 `password_plain_text`，仅为本地调试便利；后续必须切换为密码哈希。

失败响应：
- 400 `INVALID_PARAMETER`：用户名或密码为空
- 401 `AUTH_BAD_CREDENTIALS`：用户名不存在、账号不可用或密码错误

### 4.3 JWT 双令牌接口
状态：后续预留，对应交付计划阶段 6

以下接口当前不实现，不应作为联调入口：

- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

未来接入 JWT 后必须保持业务接口的用户读取方式不变，即 Controller 继续通过 `CurrentUserResolver` 获取当前用户。

## 5. 文章接口
### 5.1 创建文章
状态：已实现

```http
POST /api/v1/articles
Content-Type: application/json
X-User-Id: 3001
X-User-Nickname: Tester
```

请求体：

```json
{
  "title": "春季通勤穿搭",
  "content": "# 标题\n正文内容",
  "contentFormat": "MARKDOWN",
  "contentPreview": "可选，后端可兜底生成",
  "coverObjectKey": "article/image/2026/04/18/cover.jpg",
  "coverUrl": "https://cdn.example.com/article/image/cover.jpg",
  "images": [
    {
      "objectKey": "article/image/2026/04/18/1.jpg",
      "url": "https://cdn.example.com/article/image/1.jpg",
      "sortNo": 1,
      "cover": false
    }
  ]
}
```

请求字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `title` | 是 | 非空字符串 |
| `content` | 是 | 非空字符串，当前直接入库 |
| `contentFormat` | 是 | 当前常用 `MARKDOWN`，后续可扩展 |
| `contentPreview` | 否 | 列表摘要；不传时由内容存储网关兜底 |
| `coverObjectKey` | 否 | 封面 OSS object key |
| `coverUrl` | 否 | 封面对外访问 URL |
| `images[].objectKey` | 是 | 图片 OSS object key |
| `images[].url` | 是 | 图片对外访问 URL |
| `images[].sortNo` | 是 | 图片排序号 |
| `images[].cover` | 否 | 是否封面；兼容请求字段名 `isCover` |

成功响应：`data` 为 ArticleDetail。

失败响应：
- 401 `UNAUTHORIZED`：未传 `X-User-Id`
- 400 `INVALID_PARAMETER`：请求体校验失败
- 400 `ARTICLE_TITLE_EMPTY`：标题为空
- 400 `ARTICLE_CONTENT_EMPTY`：正文为空

### 5.2 更新文章
状态：已实现

```http
PATCH /api/v1/articles/{articleId}
Content-Type: application/json
X-User-Id: 3001
```

请求体同创建文章。

规则：
- 仅作者可更新。
- 已删除文章不可更新。
- 更新后摘要状态重置为 `PENDING`。
- 更新图片时以后端收到的 `images` 列表整体替换原文章图片。
- Feed 缓存只存文章 ID，文章卡片详情会回表读取最新标题、摘要和封面；更新文章不需要失效 inbox/outbox 时间线缓存。

成功响应：`data` 为 ArticleDetail。

失败响应：
- 401 `UNAUTHORIZED`：未传 `X-User-Id`
- 403 `ARTICLE_AUTHOR_MISMATCH`：非作者更新
- 404 `ARTICLE_NOT_FOUND`：文章不存在、ID 非法或文章已删除
- 400 `INVALID_PARAMETER` / `ARTICLE_TITLE_EMPTY` / `ARTICLE_CONTENT_EMPTY`

### 5.3 删除文章
状态：已实现

```http
DELETE /api/v1/articles/{articleId}
X-User-Id: 3001
```

成功响应：`data` 为 `null`。

规则：
- 仅作者可删除。
- 当前为软删除：`article.status` 从 `1` 改为 `0`。
- 删除成功后扣减作者 `user_profile.article_count`。
- 删除成功后删除 MySQL `article_outbox` / `user_inbox` 中该文章的时间线记录。
- 删除成功后失效作者 outbox 缓存，并失效当前粉丝的 inbox 缓存；下一次读取从 MySQL 重建，保证缓存最终一致。
- 已删除文章不会出现在文章详情、公共 Feed、关注 Feed、点赞列表和收藏列表中。

失败响应：
- 401 `UNAUTHORIZED`：未传 `X-User-Id`
- 403 `ARTICLE_AUTHOR_MISMATCH`：非作者删除
- 404 `ARTICLE_NOT_FOUND`：文章不存在、ID 非法或文章已删除

### 5.4 获取文章详情
状态：已实现

```http
GET /api/v1/articles/{articleId}
```

可选请求头：

```http
X-User-Id: 3001
```

成功响应：`data` 为 ArticleDetail。

规则：
- 未登录也可读取。
- 登录时返回当前用户对该文章的 `liked`、`favorited` 状态。
- 当前 DB 正文模式下 `contentObjectKey`、`contentUrl` 返回 `null`。
- 已删除文章不可读取。

失败响应：
- 404 `ARTICLE_NOT_FOUND`：文章不存在、ID 非法或文章已删除

### 5.5 首页公共 Feed
状态：已实现，对应交付计划阶段 5

```http
GET /api/v1/articles?pageNo=1&pageSize=10
```

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `pageNo` | 否 | 默认 `1` |
| `pageSize` | 否 | 默认 `10`，超过 `20` 时当前实现会裁剪为 `20` |
| `authorId` | 否 | 按作者 ID 过滤文章 |

成功响应：`data` 为 `PageResponse<ArticleCard>`。

当前实现说明：
- 这是公共/发现 Feed，未登录也可读取。
- 当前走 MySQL 基础分页，查询全站已发布文章。
- 默认按 `published_at DESC, id DESC` 排序。
- 登录时卡片会返回当前用户的 `liked` / `favorited` 状态。
- 该接口后续可演进为最新/热度/推荐排序，但不承载关注收件箱语义。
- 关注 Feed 使用 `GET /api/v1/users/me/feed`。

失败响应：
- 400 `INVALID_PARAMETER`：分页参数小于 `1`

## 6. 上传接口
### 6.1 创建上传任务
状态：已实现

```http
POST /api/v1/upload-tasks
Content-Type: application/json
X-User-Id: 3001
```

请求体：

```json
{
  "bizType": "ARTICLE_IMAGE",
  "fileName": "cover.jpg",
  "contentType": "image/jpeg",
  "contentLength": 1024
}
```

请求字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `bizType` | 是 | `ARTICLE_IMAGE`、`AVATAR`、`ARTICLE_CONTENT` |
| `fileName` | 是 | 原始文件名，非空 |
| `contentType` | 是 | MIME 类型，非空 |
| `contentLength` | 是 | 文件大小，必须大于 `0` |

成功响应：`data` 为 UploadTask，`status=CREATED`。

说明：
- 当前正文主流程不依赖 `ARTICLE_CONTENT`，但保留该类型作为未来正文切 OSS 的兼容预留。
- 当前只生成 OSS 直传任务，不接收二进制文件。

失败响应：
- 401 `UNAUTHORIZED`：未传 `X-User-Id`
- 400 `INVALID_PARAMETER`：请求体校验失败

### 6.2 确认上传完成
状态：已实现

```http
POST /api/v1/upload-tasks/{uploadTaskId}/complete
Content-Type: application/json
X-User-Id: 3001
```

请求体可为空；需要回传 OSS etag 时使用：

```json
{
  "etag": "etag-upload-test"
}
```

成功响应：`data` 为 UploadTask，`status=CONFIRMED`。

当前完成响应说明：
- `uploadUrl`、`uploadMethod` 不再返回新签名，当前全局 JSON 配置会省略这些 `null` 字段。
- `headers`、`formFields` 返回空对象。
- 当前 MVP 只确认任务状态，不校验 OSS 中对象是否真实存在。

失败响应：
- 401 `UNAUTHORIZED`：未传 `X-User-Id`
- 403 `FORBIDDEN`：确认他人的上传任务
- 404 `UPLOAD_TASK_NOT_FOUND`：任务不存在或 ID 非法

## 7. 互动接口
### 7.1 点赞文章
状态：已实现，对应交付计划阶段 3

```http
PUT /api/v1/articles/{articleId}/like
X-User-Id: 3001
```

成功响应：

```json
{
  "code": "0",
  "message": "success",
  "requestId": "req-xxx",
  "data": {
    "articleId": "10001",
    "liked": true,
    "likeCount": 1
  }
}
```

规则：
- 请求幂等：重复点赞不会重复计数。
- 当前 MVP 使用 `article_like` 关系表实现。
- 详情页的点赞数和当前用户点赞状态以关系表聚合为准。

失败响应：
- 401 `UNAUTHORIZED`
- 404 `ARTICLE_NOT_FOUND`

### 7.2 取消点赞
状态：已实现，对应交付计划阶段 3

```http
DELETE /api/v1/articles/{articleId}/like
X-User-Id: 3001
```

成功响应：

```json
{
  "code": "0",
  "message": "success",
  "requestId": "req-xxx",
  "data": {
    "articleId": "10001",
    "liked": false,
    "likeCount": 0
  }
}
```

规则：
- 请求幂等：重复取消点赞保持未点赞状态。

失败响应：
- 401 `UNAUTHORIZED`
- 404 `ARTICLE_NOT_FOUND`

### 7.3 收藏文章
状态：已实现，对应交付计划阶段 3

```http
PUT /api/v1/articles/{articleId}/favorite
X-User-Id: 3001
```

成功响应：

```json
{
  "code": "0",
  "message": "success",
  "requestId": "req-xxx",
  "data": {
    "articleId": "10001",
    "favorited": true,
    "favoriteCount": 1
  }
}
```

规则：
- 请求幂等：重复收藏不会重复计数。
- 当前 MVP 使用 `article_favorite` 关系表实现。
- 详情页的收藏数和当前用户收藏状态以关系表聚合为准。

失败响应：
- 401 `UNAUTHORIZED`
- 404 `ARTICLE_NOT_FOUND`

### 7.4 取消收藏
状态：已实现，对应交付计划阶段 3

```http
DELETE /api/v1/articles/{articleId}/favorite
X-User-Id: 3001
```

成功响应：

```json
{
  "code": "0",
  "message": "success",
  "requestId": "req-xxx",
  "data": {
    "articleId": "10001",
    "favorited": false,
    "favoriteCount": 0
  }
}
```

规则：
- 请求幂等：重复取消收藏保持未收藏状态。

失败响应：
- 401 `UNAUTHORIZED`
- 404 `ARTICLE_NOT_FOUND`

## 8. 关注接口
### 8.1 关注用户
状态：已实现，对应交付计划阶段 4

```http
PUT /api/v1/users/{userId}/follow
X-User-Id: 3001
```

成功响应：

```json
{
  "code": "0",
  "message": "success",
  "requestId": "req-xxx",
  "data": {
    "userId": "3002",
    "followed": true
  }
}
```

当前实现说明：
- 当前已做自关注校验。
- 使用 MySQL `user_follow` 实现关注关系。
- 请求幂等：重复关注不重复写入、不重复计数。
- 关注成功后同步维护 `user_profile.following_count` 和 `user_profile.follower_count`。
- 用户主页和文章作者信息返回当前用户的关注状态。
- 保留后续 Outbox + Canal + Kafka 事件链路扩展点，外部响应结构不变。

失败响应：
- 401 `UNAUTHORIZED`
- 400 `INVALID_PARAMETER`
- 400 `FOLLOW_SELF_NOT_ALLOWED`
- 404 `RESOURCE_NOT_FOUND`：当前用户或目标用户资料不存在

### 8.2 取消关注
状态：已实现，对应交付计划阶段 4

```http
DELETE /api/v1/users/{userId}/follow
X-User-Id: 3001
```

成功响应：

```json
{
  "code": "0",
  "message": "success",
  "requestId": "req-xxx",
  "data": {
    "userId": "3002",
    "followed": false
  }
}
```

当前实现说明：
- 当前已做自关注校验。
- 使用 MySQL `user_follow` 实现取关关系状态切换。
- 请求幂等：重复取关保持未关注状态，不重复扣减计数。
- 取关成功后同步维护 `user_profile.following_count` 和 `user_profile.follower_count`。
- 用户主页和文章作者信息返回当前用户的关注状态。

失败响应：
- 401 `UNAUTHORIZED`
- 400 `INVALID_PARAMETER`
- 400 `FOLLOW_SELF_NOT_ALLOWED`
- 404 `RESOURCE_NOT_FOUND`：当前用户或目标用户资料不存在

### 8.3 获取关注列表
状态：已实现，对应交付计划阶段 4

```http
GET /api/v1/users/{userId}/following?pageNo=1&pageSize=10
X-User-Id: 3001
```

说明：
- `X-User-Id` 可选；传入时，列表项 `followed` 表示当前访问者是否关注该列表用户。
- 未登录时，列表项 `followed=false`。
- 仅返回 `user_follow.status=1` 且 `user_profile.status=1` 的用户。
- 排序：按关注关系 `updated_at DESC, id DESC`。

成功响应：`data` 为 `PageResponse<UserProfile>`。

```json
{
  "code": "0",
  "message": "success",
  "requestId": "req-xxx",
  "data": {
    "pageNo": 1,
    "pageSize": 10,
    "total": 1,
    "records": [
      {
        "id": "3002",
        "nickname": "Followed User",
        "avatarUrl": "https://cdn.example.com/avatar.jpg",
        "bio": "个人简介",
        "followerCount": 12,
        "followingCount": 3,
        "articleCount": 5,
        "followed": true
      }
    ]
  }
}
```

失败响应：
- 400 `INVALID_PARAMETER`
- 404 `RESOURCE_NOT_FOUND`：用户资料不存在或不可用

### 8.4 获取粉丝列表
状态：已实现，对应交付计划阶段 4

```http
GET /api/v1/users/{userId}/followers?pageNo=1&pageSize=10
X-User-Id: 3001
```

说明：
- `X-User-Id` 可选；传入时，列表项 `followed` 表示当前访问者是否关注该粉丝用户。
- 未登录时，列表项 `followed=false`。
- 仅返回 `user_follow.status=1` 且 `user_profile.status=1` 的用户。
- 排序：按关注关系 `updated_at DESC, id DESC`。

成功响应：`data` 为 `PageResponse<UserProfile>`，结构同 8.3。

失败响应：
- 400 `INVALID_PARAMETER`
- 404 `RESOURCE_NOT_FOUND`：用户资料不存在或不可用

## 9. 用户接口
### 9.1 获取用户主页
状态：已实现

```http
GET /api/v1/users/{userId}
```

成功响应：

```json
{
  "code": "0",
  "message": "success",
  "requestId": "req-xxx",
  "data": {
    "id": "3002",
    "nickname": "Tester",
    "avatarUrl": "https://cdn.example.com/avatar/3002.jpg",
    "bio": "个人简介",
    "followerCount": 0,
    "followingCount": 0,
    "articleCount": 0,
    "followed": false
  }
}
```

当前实现说明：
- 当前已从 `user_profile` 返回真实昵称、头像、简介、关注数、粉丝数和文章数。
- 传入 `X-User-Id` 时会聚合 `user_follow`，返回当前用户是否已关注该主页用户。
- 未登录或访问自己的主页时，`followed=false`。

失败响应：
- 400 `INVALID_PARAMETER`：`userId` 不是数字
- 404 `RESOURCE_NOT_FOUND`：用户资料不存在或不可用

### 9.2 编辑我的个人信息
状态：已实现

```http
PUT /api/v1/users/me/profile
X-User-Id: 3001
Content-Type: application/json
```

请求体：

```json
{
  "nickname": "新的昵称",
  "avatarUrl": "https://cdn.example.com/avatar/3001.jpg",
  "bio": "新的个人简介"
}
```

字段约束：
- `nickname` 必填，不能为空，最长 64。
- `avatarUrl` 可选，最长 512；传空字符串会清空头像。
- `bio` 可选，最长 255；传空字符串会清空简介。
- 该接口只允许修改当前登录用户自己的展示资料，不允许修改粉丝数、关注数、文章数、登录用户名或密码。

成功响应：`data` 为更新后的 `UserProfile`。

失败响应：
- 401 `UNAUTHORIZED`
- 400 `INVALID_PARAMETER`
- 404 `RESOURCE_NOT_FOUND`：当前用户资料不存在或不可用

### 9.3 获取用户已发布文章
状态：已实现，对应交付计划阶段 5

```http
GET /api/v1/users/{userId}/articles?pageNo=1&pageSize=10
```

成功响应：`data` 为 `PageResponse<ArticleCard>`。

当前实现说明：
- 当前查询指定作者已发布文章列表。
- 当前按 `published_at DESC, id DESC` 排序。
- 登录时卡片会返回当前用户的 `liked` / `favorited` 状态。
- 列表只返回 `contentPreview`，不返回正文全文。

失败响应：
- 400 `INVALID_PARAMETER`：分页参数非法

### 9.4 获取我赞过的文章
状态：已实现，对应交付计划阶段 3 的沉淀列表

```http
GET /api/v1/users/me/liked-articles?pageNo=1&pageSize=10
X-User-Id: 3001
```

成功响应：`data` 为 `PageResponse<ArticleCard>`。

当前实现说明：
- 当前基于 `article_like` 查询当前用户生效中的点赞文章。
- 当前按点赞关系 `updated_at DESC, id DESC` 排序。
- 返回文章卡片、作者、摘要、互动计数和当前用户互动状态。
- 已取消点赞的文章不会出现在列表中。

失败响应：
- 401 `UNAUTHORIZED`
- 400 `INVALID_PARAMETER`

### 9.5 获取我收藏的文章
状态：已实现，对应交付计划阶段 3 的沉淀列表

```http
GET /api/v1/users/me/favorited-articles?pageNo=1&pageSize=10
X-User-Id: 3001
```

成功响应：`data` 为 `PageResponse<ArticleCard>`。

当前实现说明：
- 当前基于 `article_favorite` 查询当前用户生效中的收藏文章。
- 当前按收藏关系 `updated_at DESC, id DESC` 排序。
- 返回文章卡片、作者、摘要、互动计数和当前用户互动状态。
- 已取消收藏的文章不会出现在列表中。

失败响应：
- 401 `UNAUTHORIZED`
- 400 `INVALID_PARAMETER`

### 9.6 获取我的关注 Feed
状态：已实现，Feed 扩展能力，当前采用 MySQL 收件箱/发件箱写扩散，并已接入 Redis ZSet 缓存层

```http
GET /api/v1/users/me/feed?pageNo=1&pageSize=10
X-User-Id: 3001
```

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `pageNo` | 否 | 默认 `1` |
| `pageSize` | 否 | 默认 `10`，最大 `20` |

成功响应：`data` 为 `PageResponse<ArticleCard>`。

当前实现说明：
- 该接口是登录用户的关注 Feed，也就是收件箱流。
- 作者发布文章时，请求事务只写文章事实和 `event_outbox`；后台 worker 消费 `ArticlePublished` 事件后写入作者 `article_outbox`，并投递到粉丝 `user_inbox`。
- 用户关注作者时，请求事务只写关注关系和 `event_outbox`；后台 worker 消费 `FollowRelationshipChanged` 事件后，把该作者最近文章回填到当前用户 `user_inbox`。
- 用户取关作者时，请求事务只写关注关系和 `event_outbox`；后台 worker 消费事件后删除当前用户收件箱中该作者的文章记录。
- 当前按 `published_at DESC, article_id DESC` 排序。
- 返回文章卡片、作者、摘要、互动计数和当前用户互动状态。
- MySQL `user_inbox` / `article_outbox` 仍是事实数据源；Redis ZSet 只作为读路径缓存。
- 缓存开启时，读接口优先从 Redis 收件箱 ZSet 取文章 ID，再批量回表加载文章卡片详情；缓存未命中或 Redis 异常时自动降级到 MySQL。
- MySQL 降级查询成功后，会按 `noteit.feed.cache.rebuild-limit` 回填当前用户最近收件箱到 Redis，便于后续请求命中。
- 关注作者时，系统会优先使用作者 Redis 发件箱 ZSet 做回填；发件箱缓存未命中时从 MySQL `article_outbox` 读取最近窗口并重建 Redis。
- 发布/关注/取关会通过 DB outbox worker 异步维护或失效相关缓存；接口响应结构不变，前端无需改动，但关注 Feed 是最终一致。

失败响应：
- 401 `UNAUTHORIZED`
- 400 `INVALID_PARAMETER`
- 404 `RESOURCE_NOT_FOUND`：当前用户资料不存在或不可用

## 10. 交付计划与接口对应关系
| 交付阶段 | 接口契约 | 当前代码状态 |
| --- | --- | --- |
| 阶段 1：文档与抽象边界 | 本文档第 1-4 章；认证、Feed、互动、关注抽象边界 | 已建立 |
| 阶段 2：认证边界稳定化 | 明文密码登录；请求头模拟鉴权；`X-User-Id`、`X-User-Nickname`；JWT 接口后续预留 | 已实现 |
| 阶段 3：互动 MVP | 点赞/取消点赞、收藏/取消收藏、我赞过的/我收藏的列表 | 已实现 |
| 阶段 4：关注 MVP | 关注/取关、关注列表、粉丝列表、用户主页 `followed` 状态、作者 `followed` 状态 | 已实现 |
| 阶段 5：Feed MVP | 首页公共 Feed、用户已发布文章、编辑个人信息、基础分页排序 | 已实现基础 MySQL 查询 |
| Feed 扩展：关注收件箱 | `GET /users/me/feed`；发布写 outbox；关注回填 inbox；取关清理 inbox | 已实现 DB outbox 异步写扩散 + Redis ZSet 缓存层 |
| 阶段 6：认证正式化 | 登录响应增加 JWT；刷新、登出；业务接口迁移到 JWT 鉴权 | 仅后续预留 |

## 11. 后续演进约束
- 外部响应外层 `code/message/requestId/data` 不变。
- ArticleDetail 和 ArticleCard 的字段名不因内部存储迁移而变化。
- 正文从 DB 切到 OSS 时，详情仍返回 `content`；`contentObjectKey`、`contentUrl` 从 `null` 变为真实兼容字段。
- 点赞从 MySQL 切到 Bitmap、收藏从 MySQL 切到 ZSet 时，互动接口响应不变。
- 公共 Feed 可以独立演进为最新、热度或推荐排序；关注 Feed 使用收件箱/发件箱读模型。
- Feed 从 MySQL 收件箱/发件箱切到 Redis ZSet、缓存和游标化读模型时，分页响应结构不变；若新增 cursor，需要以可选字段或新版本接口方式演进。
- 关注从 MySQL 强一致切到 Outbox + Canal + Kafka 事件链路时，关注/取关接口响应不变。
