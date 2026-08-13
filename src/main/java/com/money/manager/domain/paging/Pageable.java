package com.money.manager.domain.paging;

public record Pageable(int page, int size, String sortBy, SortDirection direction) {

    public static Pageable of(int page, int size, String sortBy, SortDirection direction) {
        return new Pageable(Math.max(page, 0), Math.min(size, 100), sortBy, direction);
    }
}
