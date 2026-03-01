package com.catanio.ecommerce.application.product;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductFilter(
        UUID categoryId,
        String name,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
    public boolean hasCategoryFilter() {
        return categoryId != null;
    }

    public boolean hasNameFilter() {
        return name != null && !name.isBlank();
    }

    public boolean hasPriceFilter() {
        return minPrice != null && maxPrice != null;
    }
}
