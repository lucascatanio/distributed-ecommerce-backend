package com.catanio.ecommerce.domain.catalog;

import com.catanio.ecommerce.domain.shared.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateProductWithValidData() {
        var category = Category.create("Electronics", "Electronic devices");
        var product = Product.create("Notebook", "Laptop 15 inch",
                Money.of("2999.99"), 10, category);

        assertThat(product.getName()).isEqualTo("Notebook");
        assertThat(product.getPrice()).isEqualTo(Money.of("2999.99"));
        assertThat(product.getStockQuantity()).isEqualTo(10);
        assertThat(product.isActive()).isTrue();
        assertThat(product.isAvailable()).isTrue();
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        var category = Category.create("Electronics", null);
        assertThatThrownBy(() ->
                Product.create("  ", null, Money.of("100"), 10, category)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenStockIsNegative() {
        var category = Category.create("Electronics", null);
        assertThatThrownBy(() ->
                Product.create("Notebook", null, Money.of("100"), -1, category)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void shouldThrowWhenCategoryIsNull() {
        assertThatThrownBy(() ->
                Product.create("Notebook", null, Money.of("100"), 10, null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category");
    }

    @Test
    void shouldSoftDeleteProduct() {
        var category = Category.create("Electronics", null);
        var product = Product.create("Notebook", null, Money.of("100"), 5, category);

        product.softDelete();

        assertThat(product.isActive()).isFalse();
        assertThat(product.isAvailable()).isFalse();
        assertThat(product.getDeletedAt()).isNotNull();
    }

    @Test
    void shouldAdjustStock() {
        var category = Category.create("Electronics", null);
        var product = Product.create("Notebook", null, Money.of("100"), 10, category);

        product.adjustStock(-3);
        assertThat(product.getStockQuantity()).isEqualTo(7);

        product.adjustStock(5);
        assertThat(product.getStockQuantity()).isEqualTo(12);
    }

    @Test
    void shouldThrowWhenStockGoesBelowZero() {
        var category = Category.create("Electronics", null);
        var product = Product.create("Notebook", null, Money.of("100"), 5, category);

        assertThatThrownBy(() -> product.adjustStock(-10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void shouldNotBeAvailableWhenOutOfStock() {
        var category = Category.create("Electronics", null);
        var product = Product.create("Notebook", null, Money.of("100"), 0, category);

        assertThat(product.isActive()).isTrue();
        assertThat(product.isAvailable()).isFalse();
    }
}
