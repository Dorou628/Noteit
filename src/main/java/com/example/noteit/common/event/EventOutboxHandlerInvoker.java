package com.example.noteit.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 作用：根据事件类型选择对应的业务处理器，并统一执行事件消费逻辑。
 */
@Component
public class EventOutboxHandlerInvoker {

    private static final Logger log = LoggerFactory.getLogger(EventOutboxHandlerInvoker.class);

    private final List<EventOutboxHandler> handlers;

    /**
     * 作用：注入系统内所有 outbox 事件处理器。
     * 输入：handlers 为 Spring 收集到的处理器列表。
     * 输出：构造完成后的调用器实例。
     */
    public EventOutboxHandlerInvoker(List<EventOutboxHandler> handlers) {
        this.handlers = handlers;
    }

    /**
     * 作用：查找支持当前事件类型的处理器并执行。
     * 输入：event 为需要消费的 outbox 事件。
     * 输出：无返回值；没有匹配处理器时仅记录日志，处理器执行失败时向外抛出异常。
     */
    public void invoke(EventOutboxDO event) {
        Optional<EventOutboxHandler> handler = handlers.stream()
                .filter(candidate -> candidate.supports(event.eventType()))
                .findFirst();
        if (handler.isPresent()) {
            handler.get().handle(event);
        } else {
            log.info("Ignore outbox event with unsupported type={}", event.eventType());
        }
    }
}
