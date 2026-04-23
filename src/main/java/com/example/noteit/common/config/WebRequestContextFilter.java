package com.example.noteit.common.config;

import com.example.noteit.common.context.RequestContextHolder;
import com.example.noteit.common.context.UserContext;
import com.example.noteit.common.context.UserContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class WebRequestContextFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_NICKNAME_HEADER = "X-User-Nickname";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String requestId = resolveRequestId(request);
            RequestContextHolder.setRequestId(requestId);
            response.setHeader(REQUEST_ID_HEADER, requestId);

            resolveUserContext(request).ifPresent(UserContextHolder::set);
            filterChain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
            UserContextHolder.clear();
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        return StringUtils.hasText(requestId) ? requestId : "req-" + UUID.randomUUID();
    }

    private java.util.Optional<UserContext> resolveUserContext(HttpServletRequest request) {
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        if (!StringUtils.hasText(userIdHeader)) {
            return java.util.Optional.empty();
        }
        Long userId = Long.parseLong(userIdHeader);
        String nickname = request.getHeader(USER_NICKNAME_HEADER);
        return java.util.Optional.of(new UserContext(userId, nickname));
    }
}
