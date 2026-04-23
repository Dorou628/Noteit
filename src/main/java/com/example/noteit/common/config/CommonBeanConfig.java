package com.example.noteit.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class CommonBeanConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
