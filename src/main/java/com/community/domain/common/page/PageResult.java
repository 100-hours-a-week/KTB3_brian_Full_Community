package com.community.domain.common.page;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        long totalElements,
        int totalPages
) { }
