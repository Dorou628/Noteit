package com.example.noteit.common.event;

/**
 * 作用：定义 worker 投递到 Kafka 的 outbox 消息结构，只携带业务消费所需字段。
 * 输入：record 字段来自 event_outbox 数据库记录。
 * 输出：序列化后作为 Kafka 消息体，反序列化后可还原为 EventOutboxDO。
 */
public record EventOutboxKafkaMessage(
        long outboxId,
        String eventId,
        String eventType,
        String aggregateType,
        String aggregateId,
        String payload
) {

    /**
     * 作用：把数据库 outbox 记录转换为 Kafka 消息。
     * 输入：event 为数据库中的 outbox 记录。
     * 输出：可序列化到 Kafka 的消息对象。
     */
    static EventOutboxKafkaMessage from(EventOutboxDO event) {
        return new EventOutboxKafkaMessage(
                event.id(),
                event.eventId(),
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                event.payload()
        );
    }

    /**
     * 作用：把 Kafka 消息转换回业务处理器可识别的 outbox 事件对象。
     * 输入：当前 Kafka 消息 record。
     * 输出：EventOutboxDO；状态字段仅用于内存消费，不代表数据库最终状态。
     */
    EventOutboxDO toEventOutboxDO() {
        return new EventOutboxDO(
                outboxId,
                eventId,
                eventType,
                aggregateType,
                aggregateId,
                payload,
                EventOutboxStatus.SENT,
                0,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
