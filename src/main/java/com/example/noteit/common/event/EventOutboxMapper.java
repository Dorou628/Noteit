package com.example.noteit.common.event;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EventOutboxMapper {

    int insert(EventOutboxDO event);

    List<EventOutboxDO> findPending(
            @Param("now") LocalDateTime now,
            @Param("limit") int limit
    );

    int markSent(@Param("id") long id);

    int markFailed(
            @Param("id") long id,
            @Param("nextRetryAt") LocalDateTime nextRetryAt,
            @Param("lastError") String lastError
    );
}
