package com.catanio.ecommerce.domain.order;

import com.catanio.ecommerce.domain.order.events.OrderCreatedEvent;
import com.catanio.ecommerce.domain.shared.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user_id_created_at", columnList = "user_id, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    public static Order create(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("UserId is required");
        var order = new Order();
        order.userId = userId;
        order.status = OrderStatus.PENDING;
        order.createdAt = Instant.now();
        order.updatedAt = Instant.now();
        return order;
    }

    public void addItem(UUID productId, String productName, Money unitPrice, int quantity) {
        var item = OrderItem.create(this, productId, productName, unitPrice, quantity);
        this.items.add(item);
        this.updatedAt = Instant.now();
    }

    public Money calculateTotal() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(Money.zero(), Money::add);
    }

    public void confirm() {
        validateCanTransition(OrderStatus.PENDING);
        this.status = OrderStatus.PAYMENT_PROCESSING;
        this.updatedAt = Instant.now();
        domainEvents.add(new OrderCreatedEvent(this.id, this.userId, calculateTotal()));
    }

    public void markAsPaid() {
        validateCanTransition(OrderStatus.PAYMENT_PROCESSING);
        this.status = OrderStatus.PAID;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order in status: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public List<Object> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    private void validateCanTransition(OrderStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "Invalid transition. Expected: %s, Current: %s".formatted(expected, this.status)
            );
        }
    }

    @PrePersist
    private void prePersist() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
