package com.example.noteit.common.response;

import java.util.List;

public record PageResponse<T>(
        int pageNo,
        int pageSize,
        long total,
        List<T> records
) {

    public static <T> PageResponse<T> empty(int pageNo, int pageSize) {
        return new PageResponse<>(pageNo, pageSize, 0, List.of());
    }
}
