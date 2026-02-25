package com.catanio.ecommerce.domain.catalog;

import com.catanio.ecommerce.domain.shared.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_category_id", columnList = "category_id"),
        @Index(name = "idx_products_active",      columnList = "deleted_at"),
        @Index(name = "idx_products_catalog",     columnList = "category_id, price")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public static Product create(String name, String description, Money price,
                                 int stockQuantity, Category category) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }
        var product = new Product();
        product.name = name.trim();
        product.description = description;
        product.price = price.amount();
        product.stockQuantity = stockQuantity;
        product.category = category;
        product.createdAt = Instant.now();
        product.updatedAt = Instant.now();
        return product;
    }

    public void updateDetails(String name, String description, Money price) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be blank");
        this.name = name.trim();
        this.description = description;
        this.price = price.amount();
        this.updatedAt = Instant.now();
    }

    public void adjustStock(int quantity) {
        if (this.stockQuantity + quantity < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Available: %d, Requested: %d".formatted(this.stockQuantity, Math.abs(quantity))
            );
        }
        this.stockQuantity += quantity;
        this.updatedAt = Instant.now();
    }

    public boolean isAvailable() {
        return this.deletedAt == null && this.stockQuantity > 0;
    }

    public boolean isActive() {
        return this.deletedAt == null;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Money getPrice() {
        return Money.of(this.price);
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
