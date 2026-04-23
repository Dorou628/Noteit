package com.example.noteit.auth.service;

import com.example.noteit.auth.model.AuthUserDO;
import com.example.noteit.auth.model.LoginRequest;
import com.example.noteit.auth.model.LoginResponse;
import com.example.noteit.auth.repository.AuthUserRepository;
import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.exception.BusinessException;
import com.example.noteit.user.model.UserProfileDO;
import com.example.noteit.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {

    private static final int ACTIVE_STATUS = 1;
    private static final String HEADER_MOCK_AUTH_MODE = "HEADER_MOCK";

    private final AuthUserRepository authUserRepository;
    private final UserProfileRepository userProfileRepository;

    public AuthApplicationService(
            AuthUserRepository authUserRepository,
            UserProfileRepository userProfileRepository
    ) {
        this.authUserRepository = authUserRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public LoginResponse login(LoginRequest request) {
        AuthUserDO authUser = authUserRepository.findByUsername(request.username())
                .filter(user -> user.status() == ACTIVE_STATUS)
                .filter(user -> user.passwordPlainText().equals(request.password()))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS));

        UserProfileDO userProfile = userProfileRepository.findById(authUser.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS));

        return new LoginResponse(
                String.valueOf(userProfile.id()),
                userProfile.nickname(),
                userProfile.avatarUrl(),
                HEADER_MOCK_AUTH_MODE,
                null,
                null
        );
    }
}
