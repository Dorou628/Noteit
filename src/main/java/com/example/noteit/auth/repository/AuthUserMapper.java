package com.example.noteit.auth.repository;

import com.example.noteit.auth.model.AuthUserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthUserMapper {

    AuthUserDO findByUsername(@Param("username") String username);
}
