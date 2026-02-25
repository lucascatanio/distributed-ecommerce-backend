package com.catanio.ecommerce.domain.order.events;

import java.time.Instant;
import java.util.UUID;

public record OrderPaidEvent(
        UUID orderId,
        UUID userId,
        Instant occurredAt
) {
    public OrderPaidEvent(UUID orderId, UUID userId) {
        this(orderId, userId, Instant.now());
    }
}
