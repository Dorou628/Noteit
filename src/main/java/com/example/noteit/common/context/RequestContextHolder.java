package com.example.noteit.common.context;

import java.util.Optional;

public final class RequestContextHolder {

    private static final ThreadLocal<String> REQUEST_ID_HOLDER = new ThreadLocal<>();

    private RequestContextHolder() {
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID_HOLDER.set(requestId);
    }

    public static Optional<String> getRequestId() {
        return Optional.ofNullable(REQUEST_ID_HOLDER.get());
    }

    public static void clear() {
        REQUEST_ID_HOLDER.remove();
    }
}
