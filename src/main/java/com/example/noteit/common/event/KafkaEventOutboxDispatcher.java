package com.example.noteit.common.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 作用：把 worker 锁定到的 outbox 事件投递到 Kafka，由 Kafka 消费者异步执行真实业务处理。
 */
@Component
@ConditionalOnProperty(name = "noteit.event-outbox.dispatcher", havingValue = "kafka")
public class KafkaEventOutboxDispatcher implements EventOutboxDispatcher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    /**
     * 作用：注入 Kafka 发送器、JSON 序列化器和目标 topic。
     * 输入：kafkaTemplate 用于发送消息，objectMapper 用于序列化，topic 为 outbox Kafka topic。
     * 输出：构造完成后的 Kafka 分发器实例。
     */
    public KafkaEventOutboxDispatcher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${noteit.event-outbox.kafka.topic:noteit.event-outbox}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    /**
     * 作用：将 outbox 事件序列化后同步发送到 Kafka，确保发送成功后 worker 再标记 SENT。
     * 输入：event 为待投递事件。
     * 输出：无返回值；序列化或发送失败时抛出异常触发 worker 重试。
     */
    public void dispatch(EventOutboxDO event) {
        try {
            String payload = objectMapper.writeValueAsString(EventOutboxKafkaMessage.from(event));
            kafkaTemplate.send(topic, event.eventId(), payload).join();
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize outbox event", ex);
        }
    }
}
