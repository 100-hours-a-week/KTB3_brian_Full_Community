package com.community.domain.common.util;

import com.community.domain.common.model.BaseTimeEntity;
import com.community.domain.common.page.PageResult;
import com.community.domain.common.page.PaginationRequest;

import java.util.Comparator;
import java.util.List;

public class PageUtil {

    public static <T extends BaseTimeEntity> PageResult<T> paginate(List<T> source, PaginationRequest paginationRequest) {
        Comparator<T> comparator = Comparator.comparing(T::getCreatedAt);

        if (paginationRequest.direction() == PaginationRequest.SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        List<T> sorted = source.stream()
                .sorted(comparator)
                .toList();

        int from = paginationRequest.page() * paginationRequest.size();
        int to = Math.min(sorted.size(), from + paginationRequest.size());
        List<T> pagedItems = from >= sorted.size() ? List.of() : sorted.subList(from, to);

        int totalPages = calculateTotalPages((long) sorted.size(), paginationRequest.size());
        return new PageResult<>(pagedItems, sorted.size(), totalPages);
    }

    public static int calculateTotalPages(Long totalElements, int size) {
        if (totalElements == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / size);
    }

    public static String resolveDirection(PaginationRequest.SortDirection direction) {
        return direction == PaginationRequest.SortDirection.ASC ? "asc" : "desc";
    }
}
