package com.catanio.ecommerce.domain.order.events;

import com.catanio.ecommerce.domain.shared.Money;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID userId,
        Money totalAmount,
        Instant occurredAt
) {
    public OrderCreatedEvent(UUID orderId, UUID userId, Money totalAmount) {
        this(orderId, userId, totalAmount, Instant.now());
    }
}
