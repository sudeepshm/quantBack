package com.quantback.portfolio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositionTest {

    @Test
    @DisplayName("Calculates weighted average price when adding to position")
    void testWeightedAveragePrice() {
        Position position = new Position("NIFTY");
        position.add(10, new BigDecimal("100.00"));

        assertThat(position.getQuantity()).isEqualTo(10);
        assertThat(position.getAverageEntryPrice()).isEqualByComparingTo("100.00");

        // Add 10 more at 120
        // (10 * 100 + 10 * 120) / 20 = 110
        position.add(10, new BigDecimal("120.00"));
        assertThat(position.getQuantity()).isEqualTo(20);
        assertThat(position.getAverageEntryPrice()).isEqualByComparingTo("110.00");
    }

    @Test
    @DisplayName("Reduces position quantity accurately")
    void testReducePosition() {
        Position position = new Position("NIFTY");
        position.add(10, new BigDecimal("100.00"));
        position.reduce(4);

        assertThat(position.getQuantity()).isEqualTo(6);
        assertThat(position.isOpen()).isTrue();

        position.reduce(6);
        assertThat(position.getQuantity()).isEqualTo(0);
        assertThat(position.isOpen()).isFalse();
    }

    @Test
    @DisplayName("Throws when reducing more than available quantity")
    void testReduceOverPosition() {
        Position position = new Position("NIFTY");
        position.add(5, new BigDecimal("100.00"));

        assertThatThrownBy(() -> position.reduce(6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot reduce 6 units from position of 5 units");
    }

    @Test
    @DisplayName("Calculates unrealized P&L and market value correctly")
    void testUnrealizedPnlAndMarketValue() {
        Position position = new Position("NIFTY");
        position.add(10, new BigDecimal("100.00"));

        // Current price 115 -> PnL = (115 - 100) * 10 = 150
        assertThat(position.getUnrealizedPnl(new BigDecimal("115.00")))
                .isEqualByComparingTo("150.00");
        assertThat(position.getMarketValue(new BigDecimal("115.00")))
                .isEqualByComparingTo("1150.00");
    }
}
