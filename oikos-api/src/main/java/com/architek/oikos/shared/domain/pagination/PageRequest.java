package com.architek.oikos.shared.domain.pagination;

/**
 * Pagination request value object, reused by every feature's list queries.
 */
public record PageRequest(int pageNumber, int pageSize) {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public PageRequest {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be >= 0");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("pageSize must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    public static PageRequest of(int pageNumber, int pageSize) {
        return new PageRequest(pageNumber, pageSize);
    }

    public static PageRequest defaultRequest() {
        return new PageRequest(0, DEFAULT_PAGE_SIZE);
    }
}
