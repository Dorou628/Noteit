package com.example.noteit.common.event;

import com.example.noteit.common.util.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 作用：轮询 event_outbox 表中的待处理事件，抢占锁后交给分发器投递或本地消费。
 */
@Component
public class EventOutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(EventOutboxWorker.class);

    private final EventOutboxRepository eventOutboxRepository;
    private final EventOutboxDispatcher eventOutboxDispatcher;
    private final TimeProvider timeProvider;
    private final boolean enabled;
    private final int batchSize;
    private final int maxRetries;
    private final int lockTtlSeconds;
    private final String workerId;

    /**
     * 作用：注入 outbox 仓储、事件分发器、时间提供器以及 worker 运行参数。
     * 输入：enabled 控制是否启用轮询，batchSize 控制批量大小，maxRetries 控制死信阈值，lockTtlSeconds 控制处理锁过期时间。
     * 输出：构造完成后的 worker 实例，并生成当前进程唯一 workerId。
     */
    public EventOutboxWorker(
            EventOutboxRepository eventOutboxRepository,
            EventOutboxDispatcher eventOutboxDispatcher,
            TimeProvider timeProvider,
            @Value("${noteit.event-outbox.worker.enabled:true}") boolean enabled,
            @Value("${noteit.event-outbox.worker.batch-size:50}") int batchSize,
            @Value("${noteit.event-outbox.worker.max-retries:10}") int maxRetries,
            @Value("${noteit.event-outbox.worker.lock-ttl-seconds:60}") int lockTtlSeconds
    ) {
        this.eventOutboxRepository = eventOutboxRepository;
        this.eventOutboxDispatcher = eventOutboxDispatcher;
        this.timeProvider = timeProvider;
        this.enabled = enabled;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
        this.lockTtlSeconds = lockTtlSeconds;
        this.workerId = buildWorkerId();
    }

    @Scheduled(fixedDelayString = "${noteit.event-outbox.worker.fixed-delay:1000}")
    /**
     * 作用：定时拉取可处理的 outbox 事件并逐条分发。
     * 输入：无显式入参，运行参数来自配置项。
     * 输出：无返回值；处理结果通过数据库状态和日志体现。
     */
    public void consumePendingEvents() {
        if (!enabled) {
            return;
        }
        LocalDateTime now = timeProvider.now().toLocalDateTime();
        List<EventOutboxDO> events = eventOutboxRepository.claimPending(
                now,
                workerId,
                now.plusSeconds(Math.max(lockTtlSeconds, 1)),
                Math.max(batchSize, 1)
        );
        if (events.isEmpty()) {
            return;
        }
        long startedAt = System.nanoTime();
        int sent = 0;
        int failed = 0;
        for (EventOutboxDO event : events) {
            if (processOne(event)) {
                sent++;
            } else {
                failed++;
            }
        }
        long costMillis = (System.nanoTime() - startedAt) / 1_000_000L;
        log.info("Processed event outbox batch: claimed={}, sent={}, failed={}, costMillis={}, workerId={}",
                events.size(), sent, failed, costMillis, workerId);
    }

    /**
     * 作用：处理单条 outbox 事件，包括分发成功标记 SENT、失败重试或进入 DEAD。
     * 输入：event 为已经被当前 worker 抢占锁的事件。
     * 输出：处理成功返回 true，失败返回 false。
     */
    private boolean processOne(EventOutboxDO event) {
        try {
            eventOutboxDispatcher.dispatch(event);
            eventOutboxRepository.markSent(event.id());
            return true;
        } catch (RuntimeException ex) {
            String error = abbreviate(ex.getMessage());
            if (event.retryCount() + 1 >= maxRetries) {
                eventOutboxRepository.markDead(event.id(), error);
                log.warn("Moved outbox event to DEAD: id={}, type={}, retries={}",
                        event.id(), event.eventType(), event.retryCount() + 1, ex);
            } else {
                LocalDateTime nextRetryAt = nextRetryAt(event.retryCount());
                eventOutboxRepository.markFailed(event.id(), nextRetryAt, error);
                log.warn("Failed to process outbox event id={}, type={}, nextRetryAt={}",
                        event.id(), event.eventType(), nextRetryAt, ex);
            }
            return false;
        }
    }

    /**
     * 作用：计算下一次重试时间，使用指数退避并限制最大退避秒数。
     * 输入：retryCount 为当前已经失败的次数。
     * 输出：下一次允许重试的时间点。
     */
    private LocalDateTime nextRetryAt(int retryCount) {
        long delaySeconds = Math.min(60L, 1L << Math.min(retryCount, 6));
        return timeProvider.now().toLocalDateTime().plusSeconds(delaySeconds);
    }

    /**
     * 作用：截断异常信息，避免 last_error 字段过长。
     * 输入：message 为原始异常文本。
     * 输出：最长 512 字符的异常文本，原始为空时返回 null。
     */
    private String abbreviate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }

    /**
     * 作用：生成当前 worker 的唯一标识，便于数据库锁记录和日志排查。
     * 输入：无。
     * 输出：由主机名、JVM 进程名和随机 UUID 拼接出的 workerId。
     */
    private String buildWorkerId() {
        String host = "unknown-host";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            // 主机名获取失败时仍保留随机后缀，确保多个 workerId 不会轻易碰撞。
        }
        return host + "-" + ManagementFactory.getRuntimeMXBean().getName() + "-" + UUID.randomUUID();
    }
}
