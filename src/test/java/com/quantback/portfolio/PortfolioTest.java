package com.quantback.portfolio;

import com.quantback.order.OrderSide;
import com.quantback.trade.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortfolioTest {

    private Portfolio portfolio;
    private LocalDateTime timestamp;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio(new BigDecimal("10000.00"));
        timestamp = LocalDateTime.of(2026, 1, 1, 9, 15);
    }

    @Test
    @DisplayName("Applying BUY trade reduces cash and opens position")
    void testApplyBuyTrade() {
        // Buy 10 @ 100 with fee 10 -> Total cost = 1010
        Trade buy = new Trade("o1", "NIFTY", OrderSide.BUY, 10, new BigDecimal("100.00"), new BigDecimal("10.00"), timestamp);
        portfolio.applyTrade(buy);

        assertThat(portfolio.getCash()).isEqualByComparingTo("8990.00");
        assertThat(portfolio.hasOpenPosition("NIFTY")).isTrue();
        assertThat(portfolio.getPosition("NIFTY").get().getQuantity()).isEqualTo(10);
        assertThat(portfolio.getTrades()).containsExactly(buy);
    }

    @Test
    @DisplayName("Applying SELL trade increases cash, calculates realized PnL, and closes position")
    void testApplySellTrade() {
        Trade buy = new Trade("o1", "NIFTY", OrderSide.BUY, 10, new BigDecimal("100.00"), new BigDecimal("10.00"), timestamp);
        portfolio.applyTrade(buy);

        // Sell 10 @ 120 with fee 10 -> Gross = 1200, net proceeds = 1190
        // Realized PnL = (120 - 100) * 10 - 10 = 190
        Trade sell = new Trade("o2", "NIFTY", OrderSide.SELL, 10, new BigDecimal("120.00"), new BigDecimal("10.00"), timestamp.plusDays(1));
        portfolio.applyTrade(sell);

        // Initial 10000 - 1010 + 1190 = 10180 cash
        assertThat(portfolio.getCash()).isEqualByComparingTo("10180.00");
        assertThat(portfolio.hasOpenPosition("NIFTY")).isFalse();
        assertThat(portfolio.getRealizedPnl()).isEqualByComparingTo("190.00");
    }

    @Test
    @DisplayName("Throws exception when cash is insufficient for BUY trade")
    void testInsufficientCash() {
        Trade massiveBuy = new Trade("o1", "NIFTY", OrderSide.BUY, 1000, new BigDecimal("100.00"), BigDecimal.ZERO, timestamp);
        assertThatThrownBy(() -> portfolio.applyTrade(massiveBuy))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient cash");
    }

    @Test
    @DisplayName("Throws exception when selling without holding position")
    void testSellWithoutHolding() {
        Trade sell = new Trade("o1", "NIFTY", OrderSide.SELL, 5, new BigDecimal("100.00"), BigDecimal.ZERO, timestamp);
        assertThatThrownBy(() -> portfolio.applyTrade(sell))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot execute SELL trade");
    }

    @Test
    @DisplayName("Records accurate portfolio snapshot")
    void testSnapshot() {
        Trade buy = new Trade("o1", "NIFTY", OrderSide.BUY, 10, new BigDecimal("100.00"), BigDecimal.ZERO, timestamp);
        portfolio.applyTrade(buy);

        // Current price 110: positionsValue = 1100, cash = 9000 -> total = 10100
        portfolio.recordSnapshot(timestamp, "NIFTY", new BigDecimal("110.00"));

        assertThat(portfolio.getSnapshots()).hasSize(1);
        PortfolioSnapshot snapshot = portfolio.getSnapshots().get(0);
        assertThat(snapshot.getCash()).isEqualByComparingTo("9000.00");
        assertThat(snapshot.getPositionsValue()).isEqualByComparingTo("1100.00");
        assertThat(snapshot.getTotalValue()).isEqualByComparingTo("10100.00");
        assertThat(snapshot.getUnrealizedPnl()).isEqualByComparingTo("100.00");
    }
}
