package com.catanio.ecommerce.domain.order;

import com.catanio.ecommerce.domain.shared.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_items_cart_id", columnList = "cart_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    static CartItem create(Cart cart, UUID productId, String productName,
                           Money unitPrice, int quantity) {
        var item = new CartItem();
        item.cart = cart;
        item.productId = productId;
        item.productName = productName;
        item.unitPrice = unitPrice.amount();
        item.quantity = quantity;
        return item;
    }

    void updateQuantity(int quantity) {
        if (quantity <= 0 || quantity > 99) {
            throw new IllegalArgumentException("Quantity must be between 1 and 99");
        }
        this.quantity = quantity;
    }

    public Money subtotal() {
        return Money.of(this.unitPrice).multiply(this.quantity);
    }
}
