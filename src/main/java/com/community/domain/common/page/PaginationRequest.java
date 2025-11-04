package com.community.domain.common.page;

public record PaginationRequest(Integer page,
                                Integer size,
                                String sortBy,
                                SortDirection direction) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_BY = "createdAt";

    public PaginationRequest {
        page = normalizePage(page);
        size = normalizeSize(size);
        sortBy = normalizeSortBy(sortBy);
        direction = direction == null ? SortDirection.DESC : direction;
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return size;
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return DEFAULT_SORT_BY;
        }
        return sortBy.trim();
    }

    public enum SortDirection {
        ASC, DESC
    }
}
