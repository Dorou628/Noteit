# Noteit MVP 联调样例数据

## 1. 使用方式
开发环境启动时 Flyway 会执行：

```text
src/main/resources/db/migration/V4__seed_mvp_demo_articles.sql
src/main/resources/db/migration/V5__normalize_demo_seed_ids.sql
src/main/resources/db/migration/V6__auth_user_plain_password.sql
```

`V4` 是最初的样例数据，`V5` 会把样例数据规范成 MVP 阶段的简单递增 ID，`V6` 会补充 MVP 登录账号。若本地数据库已经跑过旧迁移，重启应用后 Flyway 会自动补跑缺失版本。

当前 ID 约定：
- MVP 默认使用本地简单递增 ID，方便联调和断言。
- 样例用户 ID：`101-103`
- 样例文章 ID：`1001-1006`
- 雪花 ID 生成器已保留在代码中，后续需要时再切换。

## 2. 样例用户
可以先调用登录接口：

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "style",
  "password": "123456"
}
```

样例账号：

| 用户 | username | password |
| --- | --- | --- |
| 穿搭研究所 | `style` | `123456` |
| 城市漫游 | `city` | `123456` |
| 效率手帐 | `notes` | `123456` |

登录成功后，前端把响应里的 `userId`、`nickname` 保存起来，后续请求继续带请求头：

| 用户 | Header |
| --- | --- |
| 穿搭研究所 | `X-User-Id: 101`，`X-User-Nickname: Noteit_穿搭研究所` |
| 城市漫游 | `X-User-Id: 102`，`X-User-Nickname: Noteit_城市漫游` |
| 效率手帐 | `X-User-Id: 103`，`X-User-Nickname: Noteit_效率手帐` |

## 3. 样例文章
| 文章 ID | 标题 | 图片数 | 摘要状态 |
| --- | --- | --- | --- |
| `1001` | 春季通勤穿搭：三件单品撑起一周造型 | 3 | `SUCCEEDED` |
| `1002` | 周末城市漫游路线：从旧书店走到河边 | 2 | `SUCCEEDED` |
| `1003` | 我的晨间 20 分钟整理法 | 2 | `PENDING` |
| `1004` | 小个子也能穿长裙：比例比长度更重要 | 2 | `SUCCEEDED` |
| `1005` | 一家适合独处的咖啡店：窗边座位很加分 | 2 | `PROCESSING` |
| `1006` | 一页纸复盘：把项目进展说清楚 | 2 | `SUCCEEDED` |

## 4. 常用联调接口
获取文章详情：

```http
GET /api/v1/articles/1001
```

带登录态获取详情，用于验证 `liked` / `favorited`：

```http
GET /api/v1/articles/1001
X-User-Id: 102
X-User-Nickname: Noteit_城市漫游
```

点赞：

```http
PUT /api/v1/articles/1006/like
X-User-Id: 102
```

收藏：

```http
PUT /api/v1/articles/1006/favorite
X-User-Id: 102
```

创建新文章时复用样例图片 URL：

```json
{
  "objectKey": "dev-seed/article/image/1001-1.jpg",
  "url": "https://picsum.photos/seed/noteit-article-1001-1/1200/900",
  "sortNo": 1,
  "cover": true
}
```

## 5. 当前限制
- 首页 Feed、用户已发布文章列表、“我赞过的”“我收藏的”已经可用于联调。
- 用户主页基础资料和 `followed` 当前用户态已经可用于联调；关注 / 取关会实时写入 `user_follow` 并维护用户计数。
- 开发库预置了 `101`、`102`、`103` 之间的关注关系，可直接联调关注列表和粉丝列表。
