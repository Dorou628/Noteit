package com.example.noteit.common.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageQuery(
        @Min(1) Integer pageNo,
        @Min(1) @Max(20) Integer pageSize
) {

    public int normalizedPageNo() {
        return pageNo == null ? 1 : pageNo;
    }

    public int normalizedPageSize() {
        return pageSize == null ? 10 : pageSize;
    }
}
