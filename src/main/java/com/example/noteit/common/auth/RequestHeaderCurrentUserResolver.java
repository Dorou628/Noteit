package com.example.noteit.common.auth;

import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.context.UserContext;
import com.example.noteit.common.context.UserContextHolder;
import com.example.noteit.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequestHeaderCurrentUserResolver implements CurrentUserResolver {

    @Override
    public Optional<UserContext> getCurrentUser() {
        // 当前 MVP 仍通过请求头注入 UserContext；
        // 后续切 JWT 时，只需要替换这里的实现，不需要改控制器代码。
        return UserContextHolder.get();
    }

    @Override
    public UserContext getRequiredUser() {
        return getCurrentUser()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    @Override
    public Long getRequiredUserId() {
        return getRequiredUser().userId();
    }
}
