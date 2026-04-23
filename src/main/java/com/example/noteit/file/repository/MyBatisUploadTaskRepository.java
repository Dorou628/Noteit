package com.example.noteit.file.repository;

import com.example.noteit.file.model.UploadTaskDO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisUploadTaskRepository implements UploadTaskRepository {

    private final UploadTaskMapper uploadTaskMapper;

    public MyBatisUploadTaskRepository(UploadTaskMapper uploadTaskMapper) {
        this.uploadTaskMapper = uploadTaskMapper;
    }

    @Override
    public void insert(UploadTaskDO uploadTask) {
        uploadTaskMapper.insert(uploadTask);
    }

    @Override
    public Optional<UploadTaskDO> findById(long uploadTaskId) {
        return Optional.ofNullable(uploadTaskMapper.findById(uploadTaskId));
    }

    @Override
    public boolean markConfirmed(long uploadTaskId, long userId, String etag) {
        return uploadTaskMapper.markConfirmed(uploadTaskId, userId, etag) > 0;
    }
}
