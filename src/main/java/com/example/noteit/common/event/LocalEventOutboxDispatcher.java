package com.example.noteit.common.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 作用：本地同步分发 outbox 事件，适合开发调试或不启用 Kafka 的运行方式。
 */
@Component
@ConditionalOnProperty(name = "noteit.event-outbox.dispatcher", havingValue = "local", matchIfMissing = true)
public class LocalEventOutboxDispatcher implements EventOutboxDispatcher {

    private final EventOutboxHandlerInvoker eventOutboxHandlerInvoker;

    /**
     * 作用：注入统一的事件处理器调用器。
     * 输入：eventOutboxHandlerInvoker 用于按事件类型寻找业务处理器。
     * 输出：构造完成后的本地分发器实例。
     */
    public LocalEventOutboxDispatcher(EventOutboxHandlerInvoker eventOutboxHandlerInvoker) {
        this.eventOutboxHandlerInvoker = eventOutboxHandlerInvoker;
    }

    @Override
    /**
     * 作用：直接在当前进程内消费 outbox 事件。
     * 输入：event 为待分发事件。
     * 输出：无返回值；业务处理失败时抛出异常，交给 worker 重试。
     */
    public void dispatch(EventOutboxDO event) {
        eventOutboxHandlerInvoker.invoke(event);
    }
}
