package com.example.noteit.auth.repository;

import com.example.noteit.auth.model.AuthUserDO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisAuthUserRepository implements AuthUserRepository {

    private final AuthUserMapper authUserMapper;

    public MyBatisAuthUserRepository(AuthUserMapper authUserMapper) {
        this.authUserMapper = authUserMapper;
    }

    @Override
    public Optional<AuthUserDO> findByUsername(String username) {
        return Optional.ofNullable(authUserMapper.findByUsername(username));
    }
}
