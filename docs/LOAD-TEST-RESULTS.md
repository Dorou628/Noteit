# 压测结果总表

这份文档是当前项目所有压测结果的统一记录入口，后续做缓存、MQ、索引优化、异步化改造时，都继续往这里追加。

## 术语说明

- `VU`
  - k6 的 `Virtual User`
  - 表示并发虚拟用户数
- `UV`
  - 一般是产品分析里的 `Unique Visitor`
  - 不属于这次 k6 压测指标

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

- 日期：`2026-04-24`
- 阶段：`MVP`
- 应用 Profile：`dev,local`
- 应用地址：`http://localhost:8081`
- 数据库：本地 `MySQL 8.0 / noteit`
- 缓存 / MQ：`无`
- 统一请求头示例：
  - `X-User-Id`
  - `X-User-Nickname`

## 关键说明

### 点赞插入主键冲突修复

- 早期纯新增点赞压测会命中 `LocalIdGenerator` 主键冲突
- 已于 `2026-04-24` 修复：
  - `LocalIdGenerator` 启动后自动同步到数据库当前最大 ID 之上
- 修复后，“纯新增点赞”压测结果才纳入正式基线

### 极限吞吐说明

- 下面的“极限吞吐”测试统一使用：
  - `SLEEP_SECONDS=0`
  - `VUS=300`
  - `DURATION=1m`
- 它的目的不是给出“稳态可承诺容量”
- 它更像是在找“本机单实例开始明显退化的拐点”

## 基线结果

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

## 极限吞吐结果

| 用例ID | 类型 | 接口 | 参数 | VU | 时长 | 停顿 | QPS | 平均耗时 | P95 | P99 | 错误率 | 结果文件 | 备注 |
| --- | --- | --- | --- | ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- |
| FEED-STRESS-001 | 极限吞吐 | `GET /api/v1/articles` | `pageNo=1,pageSize=10` | 300 | `1m` | 0s | 436.83 | 682.42ms | 947.49ms | 1.23s | 0.60% | `target/load-test/articles-feed-300vu-1m-s0.json` | 单机单实例下已明显退化，尾延迟和拒连开始出现 |
| DETAIL-STRESS-001 | 极限吞吐 | `GET /api/v1/articles/{articleId}` | `articleId=1006` | 300 | `1m` | 0s | 2307.48 | 116.95ms | 272.51ms | 437.37ms | 1.92% | `target/load-test/article-detail-1006-300vu-1m-s0.json` | 热点详情吞吐明显高于 Feed，但末段出现拒连 |
| LIKE-INSERT-STRESS-001 | 极限吞吐 | `PUT /api/v1/articles/{articleId}/like` | `articleId=1004, 新 synthetic user` | 300 | `1m` | 0s | 668.73 | 445.58ms | 625.97ms | 804.73ms | 0.45% | `target/load-test/article-like-insert-300vu-1m-s0.json` | 写入链路在 300 VU 下已接近本机极限 |

## 当前结论

### 1. 为什么别人能到几千上万 QPS

你现在这轮极限吞吐已经能看到：

- 热点详情：`2307 QPS`
- Feed：`436 QPS`
- 纯新增点赞：`668 QPS`

这说明只要去掉 `think time`，QPS 会马上上来。

### 2. 为什么 Feed 没有像详情一样高

当前 Feed 比详情重，原因通常包括：

- 列表查询返回体更大
- 要拼更多记录
- 分页查询和多条记录组装开销更高
- 当前实现不是缓存命中路径

### 3. 单机极限和稳态容量不是一回事

在 `300 VU / sleep=0` 这档：

- Feed 已经明显退化
- 详情吞吐很高，但也出现错误
- 点赞纯插入也进入高延迟区

所以这些数字更适合当“拐点参考”，而不是“稳态承诺”。

## 后续对比预留

| 用例ID | 目标改造 | 接口 | 建议对比档位 | 结果 | 备注 |
| --- | --- | --- | --- | --- | --- |
| FEED-CACHE-001 | Redis Feed 缓存 | `GET /api/v1/articles` | `100 VU / 1m / sleep=1` + `300 VU / 1m / sleep=0` | 待补 | 先对比稳态，再看极限吞吐 |
| DETAIL-CACHE-001 | 详情缓存 | `GET /api/v1/articles/{articleId}` | `100 VU / 1m / sleep=1` + `300 VU / 1m / sleep=0` | 待补 | 热点详情最适合看缓存收益 |
| LIKE-CACHE-001 | 计数缓存 / 异步写入 | `PUT /like` | `100 VU / 1m / sleep=1` + `300 VU / 1m / sleep=0` | 待补 | 建议同时对比错误率与尾延迟 |

## 建议的后续顺序

1. 先做 Feed 缓存前后对比
2. 再做详情缓存前后对比
3. 然后做点赞/收藏计数缓存或异步写入对比
4. 最后再考虑混合场景压测
