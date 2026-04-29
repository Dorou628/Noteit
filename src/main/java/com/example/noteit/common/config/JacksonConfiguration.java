package com.example.noteit.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 作用：提供全局 JSON 序列化配置，供 Web 层、Kafka outbox 和 Canal 消费链路复用。
 */
@Configuration
public class JacksonConfiguration {

    /**
     * 作用：注册 ObjectMapper Bean，避免异步消费者启动时无法注入 JSON 解析器。
     * 输入：无显式入参。
     * 输出：ObjectMapper Bean；如果 Spring Boot 或其它配置已经提供同类型 Bean，则不会重复创建。
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
