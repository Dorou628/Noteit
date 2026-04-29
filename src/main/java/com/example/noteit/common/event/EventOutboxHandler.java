package com.example.noteit.common.event;

/**
 * 作用：定义业务侧如何消费 outbox 事件，例如 feed 分发、缓存失效等。
 */
public interface EventOutboxHandler {

    /**
     * 作用：判断当前处理器是否支持某种事件类型。
     * 输入：eventType 为 outbox 中保存的事件类型。
     * 输出：支持返回 true，否则返回 false。
     */
    boolean supports(String eventType);

    /**
     * 作用：处理一条已经匹配到当前处理器的 outbox 事件。
     * 输入：event 为待处理事件。
     * 输出：无返回值；处理失败时抛出异常交给上游重试。
     */
    void handle(EventOutboxDO event);
}
