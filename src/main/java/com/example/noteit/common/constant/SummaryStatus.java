package com.example.noteit.common.constant;

public enum SummaryStatus {
    PENDING(0),
    PROCESSING(1),
    SUCCEEDED(2),
    FAILED(3);

    private final int code;

    SummaryStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static SummaryStatus fromCode(int code) {
        for (SummaryStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported summary status code: " + code);
    }
}
