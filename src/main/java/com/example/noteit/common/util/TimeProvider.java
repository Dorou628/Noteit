package com.example.noteit.common.util;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;

@Component
public class TimeProvider {

    private final Clock clock;

    public TimeProvider(Clock clock) {
        this.clock = clock;
    }

    public OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }
}
