package com.example.noteit.common.id;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class LocalIdGenerator implements IdGenerator {

    private final AtomicLong sequence = new AtomicLong(10_000L);

    @Override
    public long nextId() {
        // MVP 阶段先使用本地简单递增 ID，方便联调和测试读数。
        // 后续切雪花算法时只需要替换 IdGenerator 的 Spring Bean。
        return sequence.incrementAndGet();
    }
}
