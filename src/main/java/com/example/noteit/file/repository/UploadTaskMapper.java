package com.example.noteit.file.repository;

import com.example.noteit.file.model.UploadTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UploadTaskMapper {

    int insert(UploadTaskDO uploadTask);

    UploadTaskDO findById(@Param("id") long id);

    int markConfirmed(
            @Param("id") long id,
            @Param("userId") long userId,
            @Param("etag") String etag
    );
}
