package com.catanio.ecommerce.api.dto.product;

import com.catanio.ecommerce.api.dto.category.CategoryResponse;
import com.catanio.ecommerce.domain.catalog.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String name,
    String description,
    BigDecimal price,
    Integer stockQuantity,
    boolean available,
    CategoryResponse category,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice().amount(),
            product.getStockQuantity(),
            product.isAvailable(),
            CategoryResponse.from(product.getCategory()),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
