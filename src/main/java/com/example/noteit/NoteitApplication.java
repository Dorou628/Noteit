package com.example.noteit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NoteitApplication {

    /**
     * 作用：启动 Noteit Spring Boot 应用。
     * 输入：args 为 JVM 启动时传入的命令行参数。
     * 输出：无直接返回值，启动成功后应用监听配置的 HTTP 端口。
     */
    public static void main(String[] args) {
        SpringApplication.run(NoteitApplication.class, args);
    }

}
