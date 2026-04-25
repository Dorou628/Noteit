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

    @Test
    void syncToAtLeastShouldAdvanceSequencePastPersistedMax() {
        LocalIdGenerator generator = new LocalIdGenerator();

        generator.syncToAtLeast(12_345L);

        assertThat(generator.nextId()).isEqualTo(12_346L);
    }

    @Test
    void syncToAtLeastShouldNotMoveSequenceBackward() {
        LocalIdGenerator generator = new LocalIdGenerator();
        long first = generator.nextId();

        generator.syncToAtLeast(first - 1);

        assertThat(generator.nextId()).isEqualTo(first + 1);
    }
}
