package com.example.noteit.common.auth;

import com.example.noteit.common.context.UserContext;

import java.util.Optional;

public interface CurrentUserResolver {

    // 获取当前用户；未登录时返回空，适合“登录可选”的场景。
    Optional<UserContext> getCurrentUser();

    // 获取当前用户；未登录直接抛异常，适合必须登录的业务接口。
    UserContext getRequiredUser();

    // 业务层最常用的是用户 ID，所以单独提供一个便捷方法。
    Long getRequiredUserId();
}
