package com.example.noteit.relation.domain;

import com.example.noteit.common.constant.ErrorCode;
import com.example.noteit.common.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class RelationDomainService {

    public void validateCanFollow(long followerId, long followeeId) {
        if (followerId == followeeId) {
            throw new BusinessException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }
    }
}
