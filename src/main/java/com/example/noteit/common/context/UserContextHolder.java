package com.example.noteit.common.context;

import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.exception.BusinessException;

import java.util.Optional;

public final class UserContextHolder {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(UserContext userContext) {
        HOLDER.set(userContext);
    }

    public static Optional<UserContext> get() {
        return Optional.ofNullable(HOLDER.get());
    }

    public static Long getRequiredUserId() {
        return get()
                .map(UserContext::userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    public static void clear() {
        HOLDER.remove();
    }
}
