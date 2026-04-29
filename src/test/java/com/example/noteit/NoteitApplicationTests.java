package com.example.noteit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class NoteitApplicationTests {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 作用：验证 Spring Boot 应用上下文可以正常启动。
     * 输入：无。
     * 输出：无断言异常即表示启动成功。
     */
    @Test
    void contextLoads() {
    }

    /**
     * 作用：验证全局 ObjectMapper Bean 已注册，避免 Canal/Kafka 消费者启动时注入失败。
     * 输入：无。
     * 输出：断言 objectMapper 不为空。
     */
    @Test
    void objectMapperBeanShouldExist() {
        assertThat(objectMapper).isNotNull();
    }

}
