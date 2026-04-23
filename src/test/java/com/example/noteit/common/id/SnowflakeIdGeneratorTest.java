package com.example.noteit.common.id;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnowflakeIdGeneratorTest {

    @Test
    void nextIdShouldBeMonotonic() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1L);

        long first = generator.nextId();
        long second = generator.nextId();

        assertThat(second).isGreaterThan(first);
    }

    @Test
    void constructorShouldRejectInvalidWorkerId() {
        assertThatThrownBy(() -> new SnowflakeIdGenerator(1024L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
