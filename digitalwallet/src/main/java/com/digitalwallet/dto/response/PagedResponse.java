package com.digitalwallet.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic envelope for any paginated list response.
 * Keeps the pagination metadata consistent across all list endpoints.
 */
public record PagedResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last
) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}
