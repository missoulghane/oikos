package com.architek.oikos.shared.domain.pagination;

import java.util.List;
import java.util.function.Function;

/**
 * Generic read-only page of results, reused by every feature's list use cases.
 */
public record Page<T>(List<T> content, int pageNumber, int pageSize, long totalElements) {

    public Page {
        content = List.copyOf(content);
    }

    public static <T> Page<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        return new Page<>(content, pageNumber, pageSize, totalElements);
    }

    public int totalPages() {
        return pageSize == 0 ? 0 : (int) Math.ceil((double) totalElements / (double) pageSize);
    }

    public boolean hasNext() {
        return (long) (pageNumber + 1) * pageSize < totalElements;
    }

    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    public <R> Page<R> map(Function<T, R> mapper) {
        return new Page<>(content.stream().map(mapper).toList(), pageNumber, pageSize, totalElements);
    }
}
