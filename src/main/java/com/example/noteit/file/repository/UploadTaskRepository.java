package com.example.noteit.file.repository;

import com.example.noteit.file.model.UploadTaskDO;

import java.util.Optional;

public interface UploadTaskRepository {

    void insert(UploadTaskDO uploadTask);

    Optional<UploadTaskDO> findById(long uploadTaskId);

    boolean markConfirmed(long uploadTaskId, long userId, String etag);
}
