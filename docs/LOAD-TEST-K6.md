# K6 压测快速开始

这份文档用于说明当前项目的 k6 压测脚本、常用参数，以及“基线测试”和“极限吞吐测试”的使用方式。后续做 Redis ZSet、DB outbox 异步 fanout、详情缓存或索引优化时，尽量复用相同脚本和档位做对比。

## 已有脚本

- 公共 Feed：`load-test/k6/articles-feed.js`
  - `GET /api/v1/articles?pageNo=1&pageSize=10`
- 关注 Feed：`load-test/k6/following-feed.js`
  - `GET /api/v1/users/me/feed?pageNo=1&pageSize=10`
- 文章详情：`load-test/k6/article-detail.js`
  - `GET /api/v1/articles/{articleId}`
- 点赞切换：`load-test/k6/article-like-toggle.js`
  - `PUT /api/v1/articles/{articleId}/like`
  - `DELETE /api/v1/articles/{articleId}/like`
- 纯新增点赞：`load-test/k6/article-like-insert.js`
  - `PUT /api/v1/articles/{articleId}/like`

统一结果记录文档：`docs/LOAD-TEST-RESULTS.md`。

## 运行前准备

1. 先启动应用，并确认接口可访问。
2. 尽量固定测试环境：
   - 同一台机器
   - 同一套数据库数据
   - 同一套 JVM / MySQL / Redis 配置
3. 如果要做“极限吞吐测试”，把 `SLEEP_SECONDS=0`。

## 常用环境变量

- `BASE_URL`
  - 默认：`http://localhost:8081/api/v1`
- `VUS`
  - 并发虚拟用户数
- `DURATION`
  - 持续时间，例如 `30s`、`1m`
- `SLEEP_SECONDS`
  - 每轮请求后的停顿时间
  - `1` 更接近真实用户节奏
  - `0` 更接近纯吞吐压测

部分脚本还支持：

- `ARTICLE_ID`
- `PAGE_NO`
- `PAGE_SIZE`
- `USER_ID`
- `USER_NICKNAME`
- `USER_ID_BASE`
- `READER_USER_ID`
- `AUTHOR_USER_ID`
- `SETUP_ARTICLES`

## 公共 Feed 基线

```powershell
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e VUS=10 -e DURATION=30s -e SLEEP_SECONDS=1 .\load-test\k6\articles-feed.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e VUS=50 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\articles-feed.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e VUS=100 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\articles-feed.js
```

## 关注 Feed 基线

`following-feed.js` 的 `setup()` 会执行一次幂等关注，确保读者关注目标作者。可选地通过 `SETUP_ARTICLES` 创建测试文章，用于触发当前 fanout 链路，把作者文章写入读者 `user_inbox`。正式压测阶段只读取关注 Feed。

```powershell
New-Item -ItemType Directory -Force target\load-test | Out-Null

k6 run --quiet `
  -e BASE_URL=http://localhost:8081/api/v1 `
  -e VUS=10 `
  -e DURATION=30s `
  -e SLEEP_SECONDS=1 `
  -e SETUP_ARTICLES=10 `
  .\load-test\k6\following-feed.js `
  --summary-export target\load-test\following-feed-10vu-30s.json

k6 run --quiet `
  -e BASE_URL=http://localhost:8081/api/v1 `
  -e VUS=50 `
  -e DURATION=1m `
  -e SLEEP_SECONDS=1 `
  -e SETUP_ARTICLES=0 `
  .\load-test\k6\following-feed.js `
  --summary-export target\load-test\following-feed-50vu-1m.json

k6 run --quiet `
  -e BASE_URL=http://localhost:8081/api/v1 `
  -e VUS=100 `
  -e DURATION=1m `
  -e SLEEP_SECONDS=1 `
  -e SETUP_ARTICLES=0 `
  .\load-test\k6\following-feed.js `
  --summary-export target\load-test\following-feed-100vu-1m.json

k6 run --quiet `
  -e BASE_URL=http://localhost:8081/api/v1 `
  -e VUS=300 `
  -e DURATION=1m `
  -e SLEEP_SECONDS=0 `
  -e SETUP_ARTICLES=0 `
  .\load-test\k6\following-feed.js `
  --summary-export target\load-test\following-feed-300vu-1m-s0.json
```

## 详情与互动基线

```powershell
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e ARTICLE_ID=1006 -e VUS=100 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\article-detail.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e VUS=100 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\article-like-toggle.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e ARTICLE_ID=1004 -e USER_ID_BASE=900000000 -e VUS=100 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\article-like-insert.js
```

## Redis ZSet 对比命令

Redis ZSet 缓存层接入后，保持同一脚本和同一档位复测。建议先重启应用并确认当前 profile 已开启：

```yaml
noteit:
  feed:
    cache:
      enabled: true
```

正式记录前可以先执行一次低压预热，让收件箱 ZSet 命中：

```powershell
k6 run --quiet `
  -e BASE_URL=http://localhost:8081/api/v1 `
  -e VUS=10 `
  -e DURATION=30s `
  -e SLEEP_SECONDS=1 `
  -e SETUP_ARTICLES=0 `
  .\load-test\k6\following-feed.js
```

随后记录与基线相同的关键档位：

```powershell
k6 run --quiet `
  -e BASE_URL=http://localhost:8081/api/v1 `
  -e VUS=100 `
  -e DURATION=1m `
  -e SLEEP_SECONDS=1 `
  -e SETUP_ARTICLES=0 `
  .\load-test\k6\following-feed.js `
  --summary-export target\load-test\following-feed-zset-100vu-1m.json

k6 run --quiet `
  -e BASE_URL=http://localhost:8081/api/v1 `
  -e VUS=300 `
  -e DURATION=1m `
  -e SLEEP_SECONDS=0 `
  -e SETUP_ARTICLES=0 `
  .\load-test\k6\following-feed.js `
  --summary-export target\load-test\following-feed-zset-300vu-1m-s0.json
```

## 记录哪些指标

每次压测至少记录：

- `QPS`
- `Avg`
- `P95`
- `P99`
- `http_req_failed`
- 应用 CPU / 内存
- MySQL CPU / 慢查询 / 连接数
- Redis 命中率 / QPS / 内存
- DB outbox 积压量和处理延迟

## 如何对比

后面如果加 Redis、MQ、详情缓存、计数缓存、异步写入等，尽量保持这些条件一致再复跑：

- 同一脚本
- 同一接口参数
- 同一数据量
- 同一机器资源
- 同一 `VUS`
- 同一 `DURATION`
- 同一 `SLEEP_SECONDS`
- 同一缓存预热方式

然后主要看：

- `QPS` 有没有提升
- `P95 / P99` 有没有下降
- 错误率有没有改善
- 300 VU / sleep=0 是否推迟退化拐点

## 额外说明

- `VU` 是 k6 里的“虚拟用户”，不是产品分析里的 `UV`。
- “极限吞吐”不等于“稳态容量”。
- 如果某档位已经开始出现明显拒连、错误率上升、`P99` 急剧变坏，这一档更适合作为“拐点”记录，而不适合作为日常容量承诺。
