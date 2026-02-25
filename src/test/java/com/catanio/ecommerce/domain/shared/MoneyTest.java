package com.catanio.ecommerce.domain.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldAddTwoMoneyValues() {
        var a = Money.of("10.00");
        var b = Money.of("5.50");
        assertThat(a.add(b)).isEqualTo(Money.of("15.50"));
    }

    @Test
    void shouldMultiplyByQuantity() {
        var price = Money.of("25.00");
        assertThat(price.multiply(3)).isEqualTo(Money.of("75.00"));
    }

    @Test
    void shouldThrowOnNegativeAmount() {
        assertThatThrownBy(() -> Money.of("-0.01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void shouldNormalizeToTwoDecimalPlaces() {
        var money = Money.of("10.999");
        assertThat(money.amount().scale()).isEqualTo(2);
    }

    @Test
    void shouldThrowOnNullAmount() {
        assertThatThrownBy(() -> new Money(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }
}
