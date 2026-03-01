package com.catanio.ecommerce.api.dto.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequest(

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    String name,

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Digits(integer = 17, fraction = 2, message = "Price must have at most 2 decimal places")
    BigDecimal price,

    @NotNull(message = "Category is required")
    UUID categoryId
) {}
