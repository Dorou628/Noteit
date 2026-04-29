package com.example.noteit.common.event;

/**
 * 作用：定义 event_outbox 事件的分发入口，具体实现可以是本地调用、Kafka 投递或后续消息队列方案。
 */
public interface EventOutboxDispatcher {

    /**
     * 作用：分发一条 outbox 事件。
     * 输入：event 为从 event_outbox 表读取并锁定后的事件记录。
     * 输出：无返回值；分发失败时抛出运行时异常，由调用方决定重试或进入死信。
     */
    void dispatch(EventOutboxDO event);
}
