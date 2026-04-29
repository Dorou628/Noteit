package com.example.noteit.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 作用：消费 worker 投递到 Kafka 的 outbox 消息，并调用对应业务处理器。
 */
@Component
@ConditionalOnProperty(name = "noteit.event-outbox.dispatcher", havingValue = "kafka")
public class KafkaEventOutboxConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventOutboxConsumer.class);

    private final ObjectMapper objectMapper;
    private final EventOutboxHandlerInvoker eventOutboxHandlerInvoker;

    /**
     * 作用：注入 JSON 反序列化器和业务处理器调用器。
     * 输入：objectMapper 用于解析 Kafka 消息，eventOutboxHandlerInvoker 用于执行事件处理。
     * 输出：构造完成后的 Kafka 消费者实例。
     */
    public KafkaEventOutboxConsumer(ObjectMapper objectMapper, EventOutboxHandlerInvoker eventOutboxHandlerInvoker) {
        this.objectMapper = objectMapper;
        this.eventOutboxHandlerInvoker = eventOutboxHandlerInvoker;
    }

    @KafkaListener(
            topics = "${noteit.event-outbox.kafka.topic:noteit.event-outbox}",
            groupId = "${noteit.event-outbox.kafka.consumer-group:noteit-feed-fanout}"
    )
    /**
     * 作用：消费一条 Kafka outbox 消息并触发业务 handler。
     * 输入：rawMessage 为 Kafka 中的 JSON 字符串。
     * 输出：无返回值；解析或处理失败时抛出异常，由 Kafka 消费端重试策略处理。
     */
    public void consume(String rawMessage) {
        EventOutboxKafkaMessage message = readMessage(rawMessage);
        EventOutboxDO event = message.toEventOutboxDO();
        eventOutboxHandlerInvoker.invoke(event);
        log.info("Consumed Kafka outbox event: outboxId={}, eventType={}, aggregateId={}",
                event.id(), event.eventType(), event.aggregateId());
    }

    /**
     * 作用：把 Kafka 原始 JSON 字符串解析为 outbox 消息对象。
     * 输入：rawMessage 为 Kafka 消息体。
     * 输出：EventOutboxKafkaMessage；解析失败时抛出异常。
     */
    private EventOutboxKafkaMessage readMessage(String rawMessage) {
        try {
            return objectMapper.readValue(rawMessage, EventOutboxKafkaMessage.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid Kafka outbox message", ex);
        }
    }
}
