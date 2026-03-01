package com.catanio.ecommerce.api.dto.category;

import com.catanio.ecommerce.domain.catalog.Category;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
    UUID id,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
