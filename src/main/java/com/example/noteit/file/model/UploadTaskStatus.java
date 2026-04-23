package com.example.noteit.file.model;

public enum UploadTaskStatus {
    CREATED(0),
    UPLOADED(1),
    CONFIRMED(2),
    FAILED(3),
    EXPIRED(4);

    private final int code;

    UploadTaskStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static UploadTaskStatus fromCode(int code) {
        for (UploadTaskStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported upload task status code: " + code);
    }
}
