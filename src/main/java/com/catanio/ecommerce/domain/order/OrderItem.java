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
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_order_id", columnList = "order_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    static OrderItem create(Order order, UUID productId, String productName,
                            Money unitPrice, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        var item = new OrderItem();
        item.order = order;
        item.productId = productId;
        item.productName = productName;
        item.unitPrice = unitPrice.amount();
        item.quantity = quantity;
        return item;
    }

    public Money subtotal() {
        return Money.of(this.unitPrice).multiply(this.quantity);
    }
}
