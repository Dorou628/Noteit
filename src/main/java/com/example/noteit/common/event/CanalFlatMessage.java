package com.example.noteit.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * 作用：映射 Canal flatMessage JSON 结构，用于读取 MySQL binlog 中的表名、操作类型和行数据。
 * 输入：字段由 Canal Kafka 消息反序列化得到。
 * 输出：供 CanalEventOutboxConsumer 判断是否需要处理以及还原 event_outbox 行。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CanalFlatMessage(
        Long id,
        String database,
        String table,
        String type,
        Boolean isDdl,
        List<String> pkNames,
        List<Map<String, String>> data,
        List<Map<String, String>> old
) {
}
