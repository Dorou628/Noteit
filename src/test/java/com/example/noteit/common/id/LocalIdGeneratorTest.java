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
        long persistedMax = generator.nextId() + 100;

        generator.syncToAtLeast(persistedMax);

        assertThat(generator.nextId()).isEqualTo(persistedMax + 1);
    }

    @Test
    void syncToAtLeastShouldNotMoveSequenceBackward() {
        LocalIdGenerator generator = new LocalIdGenerator();
        long first = generator.nextId();

        generator.syncToAtLeast(first - 1);

        assertThat(generator.nextId()).isEqualTo(first + 1);
    }
}
