# 压测结果总表

这份文档是当前项目所有压测结果的统一记录入口，后续做缓存、MQ、索引优化、异步化改造时，都继续往这里追加。

## 记录原则

为了让前后对比有意义，除非“被测变量”本身就是要变化的对象，否则尽量固定这些维度：

- 同一脚本
- 同一接口和参数
- 同一数据量
- 同一机器资源
- 同一数据库环境
- 同一 `VUS`
- 同一 `DURATION`
- 同一 `SLEEP_SECONDS`

如果条件发生变化，必须写进备注列。

## 当前环境

- 日期：`2026-04-25`
- 阶段：`Feed 扩展 / MySQL 收件箱写扩散基线`
- 应用 Profile：`dev,local`
- 应用地址：`http://localhost:8081`
- 数据库：本地 `MySQL 8.0 / noteit`
- 结果文件目录：`target/load-test/`

## 公共 Feed 与互动基线

| 用例ID | 类型 | 接口 | 参数 | VU | 时长 | 停顿 | QPS | 平均耗时 | P95 | P99 | 错误率 | 结果文件 | 备注 |
| --- | --- | --- | --- | ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- |
| FEED-BASE-001 | 基线 | `GET /api/v1/articles` | `pageNo=1,pageSize=10` | 10 | `30s` | 1s | 9.67 | 29.50ms | 53.73ms | 143.16ms | 0.00% | `target/load-test/articles-feed-10vu-30s.json` | Feed 冒烟 |
| FEED-BASE-002 | 基线 | `GET /api/v1/articles` | `pageNo=1,pageSize=10` | 50 | `1m` | 1s | 48.67 | 23.33ms | 33.92ms | 85.45ms | 0.00% | `target/load-test/articles-feed-50vu-1m.json` | Feed 中压 |
| FEED-BASE-003 | 基线 | `GET /api/v1/articles` | `pageNo=1,pageSize=10` | 100 | `1m` | 1s | 96.79 | 27.21ms | 50.23ms | 192.82ms | 0.00% | `target/load-test/articles-feed-100vu-1m.json` | Feed 高压 |
| DETAIL-BASE-001 | 基线 | `GET /api/v1/articles/{articleId}` | `articleId=1006` | 10 | `30s` | 1s | 9.93 | 5.94ms | 12.16ms | 15.58ms | 0.00% | `target/load-test/article-detail-1006-10vu-30s.json` | 详情冒烟 |
| DETAIL-BASE-002 | 基线 | `GET /api/v1/articles/{articleId}` | `articleId=1006` | 50 | `1m` | 1s | 49.68 | 5.22ms | 9.28ms | 22.71ms | 0.00% | `target/load-test/article-detail-1006-50vu-1m.json` | 详情中压 |
| DETAIL-BASE-003 | 基线 | `GET /api/v1/articles/{articleId}` | `articleId=1006` | 100 | `1m` | 1s | 99.29 | 5.54ms | 13.26ms | 32.85ms | 0.00% | `target/load-test/article-detail-1006-100vu-1m.json` | 详情高压 |
| LIKE-BASE-001 | 基线 | `PUT/DELETE /api/v1/articles/{articleId}/like` | `切换已有点赞关系` | 10 | `30s` | 1s | 9.89 | 9.47ms | 11.69ms | 22.14ms | 0.00% | `target/load-test/article-like-toggle-10vu-30s.json` | 点赞切换冒烟 |
| LIKE-BASE-002 | 基线 | `PUT/DELETE /api/v1/articles/{articleId}/like` | `切换已有点赞关系` | 50 | `1m` | 1s | 49.42 | 9.12ms | 19.53ms | 49.36ms | 0.00% | `target/load-test/article-like-toggle-50vu-1m.json` | 点赞切换中压 |
| LIKE-BASE-003 | 基线 | `PUT/DELETE /api/v1/articles/{articleId}/like` | `切换已有点赞关系` | 100 | `1m` | 1s | 98.86 | 8.78ms | 19.00ms | 55.45ms | 0.00% | `target/load-test/article-like-toggle-100vu-1m.json` | 点赞切换高压 |
| LIKE-INSERT-001 | 基线 | `PUT /api/v1/articles/{articleId}/like` | `articleId=1004, 新 synthetic user` | 10 | `30s` | 1s | 9.87 | 11.84ms | 31.40ms | 46.85ms | 0.00% | `target/load-test/article-like-insert-10vu-30s.json` | 纯新增点赞冒烟 |
| LIKE-INSERT-002 | 基线 | `PUT /api/v1/articles/{articleId}/like` | `articleId=1004, 新 synthetic user` | 50 | `1m` | 1s | 49.48 | 9.26ms | 15.60ms | 28.37ms | 0.00% | `target/load-test/article-like-insert-50vu-1m.json` | 纯新增点赞中压 |
| LIKE-INSERT-003 | 基线 | `PUT /api/v1/articles/{articleId}/like` | `articleId=1004, 新 synthetic user` | 100 | `1m` | 1s | 98.64 | 11.56ms | 22.93ms | 40.78ms | 0.00% | `target/load-test/article-like-insert-100vu-1m.json` | 纯新增点赞高压 |

## 关注 Feed 基线结果

| 用例 ID | 类型 | 接口 | VU | 时长 | 停顿 | QPS | 平均耗时 | P95 | P99 | 错误率 | 结果文件 | 备注 |
| --- | --- | --- | ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- |
| FOLLOW-FEED-BASE-001 | 基线 | `GET /api/v1/users/me/feed` | 10 | `30s` | 1s | 10.06 | 22.65ms | 25.62ms | 27.39ms | 0.00% | `target/load-test/following-feed-10vu-30s.json` | 冒烟/低压 |
| FOLLOW-FEED-BASE-002 | 基线 | `GET /api/v1/users/me/feed` | 50 | `1m` | 1s | 48.52 | 25.92ms | 47.39ms | 114.51ms | 0.00% | `target/load-test/following-feed-50vu-1m.json` | 中压 |
| FOLLOW-FEED-BASE-003 | 基线 | `GET /api/v1/users/me/feed` | 100 | `1m` | 1s | 96.45 | 29.04ms | 75.76ms | 209.05ms | 0.00% | `target/load-test/following-feed-100vu-1m.json` | 高压 |
| FOLLOW-FEED-STRESS-001 | 极限吞吐 | `GET /api/v1/users/me/feed` | 300 | `1m` | 0s | 385.37 | 772.81ms | 1.03s | 1.24s | 0.00% | `target/load-test/following-feed-300vu-1m-s0.json` | 单机 MySQL inbox 读路径明显退化 |

## 极限吞吐结果

| 用例ID | 类型 | 接口 | 参数 | VU | 时长 | 停顿 | QPS | 平均耗时 | P95 | P99 | 错误率 | 结果文件 | 备注 |
| --- | --- | --- | --- | ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- |
| FEED-STRESS-001 | 极限吞吐 | `GET /api/v1/articles` | `pageNo=1,pageSize=10` | 300 | `1m` | 0s | 436.83 | 682.42ms | 947.49ms | 1.23s | 0.60% | `target/load-test/articles-feed-300vu-1m-s0.json` | 单机单实例下已明显退化，尾延迟和拒连开始出现 |
| DETAIL-STRESS-001 | 极限吞吐 | `GET /api/v1/articles/{articleId}` | `articleId=1006` | 300 | `1m` | 0s | 2307.48 | 116.95ms | 272.51ms | 437.37ms | 1.92% | `target/load-test/article-detail-1006-300vu-1m-s0.json` | 热点详情吞吐明显高于 Feed，但末段出现拒连 |
| LIKE-INSERT-STRESS-001 | 极限吞吐 | `PUT /api/v1/articles/{articleId}/like` | `articleId=1004, 新 synthetic user` | 300 | `1m` | 0s | 668.73 | 445.58ms | 625.97ms | 804.73ms | 0.45% | `target/load-test/article-like-insert-300vu-1m-s0.json` | 写入链路在 300 VU 下已接近本机极限 |

## 当前结论

- 在真实用户停顿 `SLEEP_SECONDS=1` 下，关注 Feed 10/50/100 VU 都保持 0 错误率。
- 关注 Feed 100 VU 时 QPS 约 `96.45`，P95 为 `75.76ms`，P99 为 `209.05ms`。
- `300 VU / sleep=0` 下关注 Feed QPS 约 `385.37`，但平均耗时升至 `772.81ms`，P95 超过 `1s`，已经不适合作为稳定容量承诺。
- 热点详情在极限档达到 `2307 QPS`，说明详情缓存后续适合做专项对比。

## 后续对比预留

| 用例 ID | 目标改造 | 接口 | 建议对比档位 | 结果 | 备注 |
| --- | --- | --- | --- | --- | --- |
| FOLLOW-FEED-ZSET-001 | Redis ZSet 收件箱 | `GET /api/v1/users/me/feed` | `100 VU / 1m / sleep=1` | 待复测 | 先低压预热，让收件箱 ZSet 命中后再记录 |
| FOLLOW-FEED-ZSET-002 | Redis ZSet 收件箱 | `GET /api/v1/users/me/feed` | `300 VU / 1m / sleep=0` | 待复测 | 看极限吞吐和退化拐点 |
| FOLLOW-FEED-ASYNC-001 | DB outbox 异步 fanout | 发布/删除/关注链路 | 写入压测 | 待补 | 关注接口耗时、event_outbox 积压和处理延迟 |
| DETAIL-CACHE-001 | 详情缓存 | `GET /api/v1/articles/{articleId}` | `100 VU / 1m / sleep=1` + `300 VU / 1m / sleep=0` | 待补 | 热点详情最适合看缓存收益 |
| LIKE-CACHE-001 | 计数缓存 / 异步写入 | `PUT /like` | `100 VU / 1m / sleep=1` + `300 VU / 1m / sleep=0` | 待补 | 建议同时对比错误率与尾延迟 |

## Redis ZSet 复测注意事项

- MySQL `user_inbox` / `article_outbox` 仍是事实数据源，Redis 只缓存时间线 ID。
- 首次读取某个用户收件箱时，如果 Redis key 不存在，会走 MySQL 并回填缓存；正式结果应在预热后记录。
- 当前 k6 测试曾创建大量空白文章、点赞和收藏数据。如果这些数据影响可读性，优先使用固定测试账号和固定作者复测；不要在未备份环境里直接清库。
- 对比时重点观察 `100 VU / sleep=1` 的 P95/P99，以及 `300 VU / sleep=0` 是否降低尾延迟或提升 QPS。
