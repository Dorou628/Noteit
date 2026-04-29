package com.example.noteit.common.event;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 作用：启用 Spring Kafka 注解能力，使 @KafkaListener 可以消费 outbox 和 Canal 消息。
 */
@Configuration
@EnableKafka
public class KafkaEventOutboxConfiguration {

    /**
     * 作用：注册 Kafka 消费者工厂，供 @KafkaListener 的容器工厂创建消费者实例。
     * 输入：bootstrapServers 为 Kafka 地址，来自 spring.kafka.bootstrap-servers 或默认 localhost:9092。
     * 输出：ConsumerFactory Bean，key/value 均按字符串反序列化。
     */
    @Bean
    @ConditionalOnMissingBean
    public ConsumerFactory<String, String> kafkaConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    /**
     * 作用：注册 @KafkaListener 默认使用的容器工厂，修复启动时找不到 kafkaListenerContainerFactory 的问题。
     * 输入：consumerFactory 为 Kafka 消费者工厂。
     * 输出：名为 kafkaListenerContainerFactory 的监听容器工厂 Bean。
     */
    @Bean(name = "kafkaListenerContainerFactory")
    @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    /**
     * 作用：注册 Kafka 生产者工厂，供 worker->Kafka 阶段投递 outbox 消息。
     * 输入：bootstrapServers 为 Kafka 地址，来自 spring.kafka.bootstrap-servers 或默认 localhost:9092。
     * 输出：ProducerFactory Bean，key/value 均按字符串序列化。
     */
    @Bean
    @ConditionalOnMissingBean
    public ProducerFactory<String, String> kafkaProducerFactory(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(properties);
    }

    /**
     * 作用：注册 KafkaTemplate，供 KafkaEventOutboxDispatcher 同步发送 outbox 消息。
     * 输入：producerFactory 为 Kafka 生产者工厂。
     * 输出：KafkaTemplate Bean。
     */
    @Bean
    @ConditionalOnMissingBean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
