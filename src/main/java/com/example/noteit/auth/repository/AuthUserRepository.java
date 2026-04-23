package com.example.noteit.auth.repository;

import com.example.noteit.auth.model.AuthUserDO;

import java.util.Optional;

public interface AuthUserRepository {

    Optional<AuthUserDO> findByUsername(String username);
}
