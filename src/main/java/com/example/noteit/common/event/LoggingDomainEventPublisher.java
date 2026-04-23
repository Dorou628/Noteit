package com.example.noteit.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        // TODO: 后续替换为事务后事件、Outbox 或 MQ 投递。
        log.info("Publish domain event: type={}, aggregateType={}, aggregateId={}",
                event.eventType(), event.aggregateType(), event.aggregateId());
    }
}
