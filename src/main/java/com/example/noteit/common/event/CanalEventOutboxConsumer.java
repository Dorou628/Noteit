package com.example.noteit.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 作用：消费 Canal 投递到 Kafka 的 binlog 消息，监听 event_outbox 插入事件并直接执行业务 handler。
 */
@Component
@ConditionalOnProperty(name = "noteit.canal.consumer.enabled", havingValue = "true")
public class CanalEventOutboxConsumer {

    private static final Logger log = LoggerFactory.getLogger(CanalEventOutboxConsumer.class);
    private static final String EVENT_OUTBOX_TABLE = "event_outbox";
    private static final String INSERT_TYPE = "INSERT";

    private final ObjectMapper objectMapper;
    private final EventOutboxHandlerInvoker eventOutboxHandlerInvoker;
    private final EventOutboxRepository eventOutboxRepository;

    /**
     * 作用：注入 JSON 解析器、业务处理器调用器和 outbox 仓储。
     * 输入：objectMapper 用于解析 Canal 消息，eventOutboxHandlerInvoker 用于消费事件，eventOutboxRepository 用于回写 SENT 状态。
     * 输出：构造完成后的 Canal 消费者实例。
     */
    public CanalEventOutboxConsumer(
            ObjectMapper objectMapper,
            EventOutboxHandlerInvoker eventOutboxHandlerInvoker,
            EventOutboxRepository eventOutboxRepository
    ) {
        this.objectMapper = objectMapper;
        this.eventOutboxHandlerInvoker = eventOutboxHandlerInvoker;
        this.eventOutboxRepository = eventOutboxRepository;
    }

    @KafkaListener(
            topics = "${noteit.canal.topic:noteit.canal}",
            groupId = "${noteit.canal.consumer-group:noteit-canal-outbox}"
    )
    /**
     * 作用：消费 Canal flatMessage，只处理 event_outbox 表的 INSERT 行。
     * 输入：rawMessage 为 Canal 写入 Kafka 的 JSON 字符串。
     * 输出：无返回值；业务处理成功后将对应 outbox 行标记为 SENT，失败时抛出异常等待 Kafka 重试。
     */
    public void consume(String rawMessage) {
        CanalFlatMessage message = readMessage(rawMessage);
        if (!shouldProcess(message)) {
            return;
        }
        List<Map<String, String>> rows = message.data() == null ? List.of() : message.data();
        for (Map<String, String> row : rows) {
            EventOutboxDO event = toOutboxEvent(row);
            if (event.status() != EventOutboxStatus.NEW) {
                log.info("Ignore Canal outbox row with status={}, id={}", event.status(), event.id());
                continue;
            }
            eventOutboxHandlerInvoker.invoke(event);
            eventOutboxRepository.markSent(event.id());
            log.info("Consumed Canal outbox event: outboxId={}, eventType={}, aggregateId={}",
                    event.id(), event.eventType(), event.aggregateId());
        }
    }

    /**
     * 作用：判断 Canal 消息是否属于需要消费的 event_outbox 新增事件。
     * 输入：message 为解析后的 Canal flatMessage。
     * 输出：需要处理返回 true，否则返回 false。
     */
    private boolean shouldProcess(CanalFlatMessage message) {
        return message != null
                && !Boolean.TRUE.equals(message.isDdl())
                && EVENT_OUTBOX_TABLE.equals(message.table())
                && INSERT_TYPE.equalsIgnoreCase(message.type());
    }

    /**
     * 作用：把 Canal 原始 JSON 字符串解析为 flatMessage 对象。
     * 输入：rawMessage 为 Kafka 消息体。
     * 输出：CanalFlatMessage；解析失败时抛出异常。
     */
    private CanalFlatMessage readMessage(String rawMessage) {
        try {
            return objectMapper.readValue(rawMessage, CanalFlatMessage.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid Canal flat message", ex);
        }
    }

    /**
     * 作用：把 Canal 行数据转换为 event_outbox 领域对象。
     * 输入：row 为 Canal data 数组中的单行列值映射。
     * 输出：EventOutboxDO，供业务 handler 消费和状态回写使用。
     */
    private EventOutboxDO toOutboxEvent(Map<String, String> row) {
        return new EventOutboxDO(
                parseLong(row, "id"),
                row.get("event_id"),
                row.get("event_type"),
                row.get("aggregate_type"),
                row.get("aggregate_id"),
                row.get("payload"),
                parseInt(row, "status"),
                parseInt(row, "retry_count"),
                parseDateTime(row.get("next_retry_at")),
                row.get("locked_by"),
                parseDateTime(row.get("locked_until")),
                row.get("last_error"),
                parseDateTime(row.get("created_at")),
                parseDateTime(row.get("updated_at"))
        );
    }

    /**
     * 作用：读取并解析 long 类型列。
     * 输入：row 为列值映射，key 为列名。
     * 输出：解析后的 long 值；缺失或格式错误时抛出异常。
     */
    private long parseLong(Map<String, String> row, String key) {
        return Long.parseLong(row.get(key));
    }

    /**
     * 作用：读取并解析 int 类型列，空值按 0 处理。
     * 输入：row 为列值映射，key 为列名。
     * 输出：解析后的 int 值。
     */
    private int parseInt(Map<String, String> row, String key) {
        String value = row.get(key);
        return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
    }

    /**
     * 作用：解析 Canal 输出的时间字符串。
     * 输入：value 为 Canal 中的日期时间文本。
     * 输出：LocalDateTime；空值返回 null。
     */
    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value.replace(' ', 'T'));
    }
}
