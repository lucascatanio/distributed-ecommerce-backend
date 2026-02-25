package com.catanio.ecommerce.domain.order;

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
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public static Cart create(UUID userId) {
        var cart = new Cart();
        cart.userId = userId;
        cart.updatedAt = Instant.now();
        cart.expiresAt = Instant.now().plusSeconds(7L * 24 * 60 * 60);
        return cart;
    }

    public void addOrUpdateItem(UUID productId, String productName, Money unitPrice, int quantity) {
        if (quantity <= 0 || quantity > 99) {
            throw new IllegalArgumentException("Quantity must be between 1 and 99");
        }
        items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.updateQuantity(quantity),
                        () -> items.add(CartItem.create(this, productId, productName, unitPrice, quantity))
                );
        this.updatedAt = Instant.now();
        refreshExpiration();
    }

    public void removeItem(UUID productId) {
        items.removeIf(i -> i.getProductId().equals(productId));
        this.updatedAt = Instant.now();
    }

    public void clear() {
        items.clear();
        this.updatedAt = Instant.now();
    }

    public Money calculateTotal() {
        return items.stream()
                .map(CartItem::subtotal)
                .reduce(Money.zero(), Money::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    private void refreshExpiration() {
        this.expiresAt = Instant.now().plusSeconds(7L * 24 * 60 * 60);
    }

    @PrePersist
    private void prePersist() {
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
