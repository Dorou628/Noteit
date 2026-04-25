# K6 压测快速开始

这份文档用于说明当前项目的 k6 压测脚本、常用参数，以及“基线测试”和“极限吞吐测试”的使用方式。

## 已有脚本

- `GET /api/v1/articles?pageNo=1&pageSize=10`
  - `load-test/k6/articles-feed.js`
- `GET /api/v1/articles/{articleId}`
  - `load-test/k6/article-detail.js`
- `PUT /api/v1/articles/{articleId}/like` 与 `DELETE /api/v1/articles/{articleId}/like`
  - `load-test/k6/article-like-toggle.js`
- `PUT /api/v1/articles/{articleId}/like` 纯新增点赞路径
  - `load-test/k6/article-like-insert.js`

统一结果记录文档：

- `docs/LOAD-TEST-RESULTS.md`

## 运行前准备

1. 先启动应用，并确认接口可访问。
2. 尽量固定测试环境：
   - 同一台机器
   - 同一套数据库数据
   - 同一套 JVM / MySQL 配置
3. 如果要做“极限吞吐测试”，把 `SLEEP_SECONDS=0`。

## 常用环境变量

- `BASE_URL`
  - 默认：`http://localhost:8080/api/v1`
  - 当前项目本地 `dev,local` 实际通常是：`http://localhost:8081/api/v1`
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

## 基线测试

这类测试用于做“加缓存前 / 加缓存后”的对比，建议保留真实用户停顿。

### Feed 基线

```powershell
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e VUS=10 -e DURATION=30s -e SLEEP_SECONDS=1 .\load-test\k6\articles-feed.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e VUS=50 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\articles-feed.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e VUS=100 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\articles-feed.js
```

### 文章详情基线

```powershell
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e ARTICLE_ID=1006 -e VUS=10 -e DURATION=30s -e SLEEP_SECONDS=1 .\load-test\k6\article-detail.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e ARTICLE_ID=1006 -e VUS=50 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\article-detail.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e ARTICLE_ID=1006 -e VUS=100 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\article-detail.js
```

### 点赞切换基线

```powershell
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e VUS=10 -e DURATION=30s -e SLEEP_SECONDS=1 .\load-test\k6\article-like-toggle.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e VUS=50 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\article-like-toggle.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e VUS=100 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\article-like-toggle.js
```

### 点赞纯新增基线

```powershell
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e ARTICLE_ID=1004 -e USER_ID_BASE=900000000 -e VUS=10 -e DURATION=30s -e SLEEP_SECONDS=1 .\load-test\k6\article-like-insert.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e ARTICLE_ID=1004 -e USER_ID_BASE=900000000 -e VUS=50 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\article-like-insert.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e ARTICLE_ID=1004 -e USER_ID_BASE=900000000 -e VUS=100 -e DURATION=1m -e SLEEP_SECONDS=1 .\load-test\k6\article-like-insert.js
```

## 极限吞吐测试

这类测试用于看“本机单实例被顶到什么程度会开始退化”，通常：

- `SLEEP_SECONDS=0`
- `VUS` 提高到较高水平
- 更关注吞吐上限、错误率和尾延迟恶化点

### 本次使用的极限吞吐命令

```powershell
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e VUS=300 -e DURATION=1m -e SLEEP_SECONDS=0 .\load-test\k6\articles-feed.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e ARTICLE_ID=1006 -e VUS=300 -e DURATION=1m -e SLEEP_SECONDS=0 .\load-test\k6\article-detail.js
k6 run -e BASE_URL=http://localhost:8081/api/v1 -e ARTICLE_ID=1004 -e USER_ID_BASE=900000000 -e VUS=300 -e DURATION=1m -e SLEEP_SECONDS=0 .\load-test\k6\article-like-insert.js
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

## 如何对比

后面如果你加了 Redis、MQ、详情缓存、计数缓存、异步写入等，尽量保持这些条件一致再复跑：

- 同一脚本
- 同一接口参数
- 同一数据量
- 同一机器资源
- 同一 `VUS`
- 同一 `DURATION`
- 同一 `SLEEP_SECONDS`

然后主要看：

- `QPS` 有没有提升
- `P95 / P99` 有没有下降
- 错误率有没有改善

## 额外说明

- `VU` 是 k6 里的“虚拟用户”，不是产品分析里的 `UV`
- “极限吞吐”不等于“稳态容量”
- 如果某档位已经开始出现明显拒连、错误率上升、`P99` 急剧变坏，这一档更适合作为“拐点”记录，而不适合作为日常容量承诺
