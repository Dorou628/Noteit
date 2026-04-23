package com.example.noteit.common.id;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalIdGeneratorTest {

    @Test
    void nextIdShouldBeMonotonic() {
        LocalIdGenerator generator = new LocalIdGenerator();

        long first = generator.nextId();
        long second = generator.nextId();

        assertThat(second).isGreaterThan(first);
        assertThat(second).isEqualTo(first + 1);
    }
}
