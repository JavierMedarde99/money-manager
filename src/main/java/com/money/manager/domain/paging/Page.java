package com.money.manager.domain.paging;

import java.util.List;

public record Page<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> Page<T> of(List<T> content, int page, int size, long totalElements, int totalPages) {
        return new Page<>(content, page, size, totalElements, totalPages);
    }
}
