package com.catanio.ecommerce.domain.order;

public enum OrderStatus {
    PENDING,
    PAYMENT_PROCESSING,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
