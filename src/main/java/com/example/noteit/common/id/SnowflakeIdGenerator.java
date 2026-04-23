package com.example.noteit.common.id;

public class SnowflakeIdGenerator implements IdGenerator {

    private static final long EPOCH_MILLIS = 1704067200000L; // 2024-01-01T00:00:00Z
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = (1L << WORKER_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    private final long workerId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowflakeIdGenerator(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("workerId must be between 0 and " + MAX_WORKER_ID);
        }
        this.workerId = workerId;
    }

    @Override
    public synchronized long nextId() {
        long currentTimestamp = currentTimestamp();
        if (currentTimestamp < lastTimestamp) {
            currentTimestamp = lastTimestamp;
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;
        return ((currentTimestamp - EPOCH_MILLIS) << (WORKER_ID_BITS + SEQUENCE_BITS))
                | (workerId << SEQUENCE_BITS)
                | sequence;
    }

    protected long currentTimestamp() {
        return System.currentTimeMillis();
    }

    private long waitNextMillis(long previousTimestamp) {
        long timestamp = currentTimestamp();
        while (timestamp <= previousTimestamp) {
            timestamp = currentTimestamp();
        }
        return timestamp;
    }
}
