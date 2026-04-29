package com.example.noteit.common.event;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EventOutboxMapper {

    int insert(EventOutboxDO event);

    List<EventOutboxDO> findClaimCandidates(
            @Param("now") LocalDateTime now,
            @Param("limit") int limit
    );

    int claim(
            @Param("id") long id,
            @Param("now") LocalDateTime now,
            @Param("workerId") String workerId,
            @Param("lockedUntil") LocalDateTime lockedUntil
    );

    EventOutboxDO findById(@Param("id") long id);

    int markSent(@Param("id") long id);

    int markFailed(
            @Param("id") long id,
            @Param("nextRetryAt") LocalDateTime nextRetryAt,
            @Param("lastError") String lastError
    );

    int markDead(
            @Param("id") long id,
            @Param("lastError") String lastError
    );
}
