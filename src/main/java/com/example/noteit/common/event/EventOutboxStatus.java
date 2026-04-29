package com.example.noteit.common.event;

/**
 * 作用：集中定义 event_outbox.status 的状态值，避免各处直接写魔法数字。
 */
public final class EventOutboxStatus {

    public static final int NEW = 0;
    public static final int SENT = 1;
    public static final int FAILED = 2;
    public static final int PROCESSING = 3;
    public static final int DEAD = 4;

    /**
     * 作用：工具类禁止实例化。
     * 输入：无。
     * 输出：无。
     */
    private EventOutboxStatus() {
    }
}
