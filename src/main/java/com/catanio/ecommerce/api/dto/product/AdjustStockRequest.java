package com.catanio.ecommerce.api.dto.product;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdjustStockRequest(

    @NotNull(message = "Quantity is required")
    @Min(value = -9999, message = "Quantity must be at least -9999")
    @Max(value = 9999, message = "Quantity must be at most 9999")
    Integer quantity
) {}
