package com.example.noteit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NoteitApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteitApplication.class, args);
    }

}
