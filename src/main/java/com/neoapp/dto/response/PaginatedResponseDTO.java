package com.neoapp.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaginatedResponseDTO<T>(
        boolean success,
        String message,
        List<T> content,
        PaginationInfoDTO pagination,
        String timestamp
) {
    public static <T> PaginatedResponseDTO<T> success(String message, Page<T> page) {
        return new PaginatedResponseDTO<T>(
                true,
                message,
                page.getContent(),
                new PaginationInfoDTO(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.isFirst(),
                        page.isLast(),
                        page.hasNext(),
                        page.hasPrevious()
                ),
                java.time.Instant.now().toString()
        );
    }

    public static <T> PaginatedResponseDTO<T> error(String message) {
        return new PaginatedResponseDTO<>(
                false,
                message,
                null,
                null,
                java.time.Instant.now().toString()
        );
    }
}
