package com.catanio.ecommerce.api.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
    List<T> data,
    PaginationMeta pagination
) {
    public record PaginationMeta(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
    ) {}

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            new PaginationMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
            )
        );
    }
}
