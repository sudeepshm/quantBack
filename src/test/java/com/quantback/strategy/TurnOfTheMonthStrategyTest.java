package com.quantback.strategy;

import com.quantback.data.Candle;
import com.quantback.data.MarketData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TurnOfTheMonthStrategyTest {

    @Test
    @DisplayName("Validates constructor parameters")
    void testConstructorValidation() {
        assertThatThrownBy(() -> new TurnOfTheMonthStrategy(0, 3))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TurnOfTheMonthStrategy(4, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when data is empty")
    void testEmptyData() {
        TurnOfTheMonthStrategy strategy = new TurnOfTheMonthStrategy(4, 3);
        MarketData data = new MarketData("TEST", List.of());

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY during first days of month")
    void testBuyFirstDaysOfMonth() {
        TurnOfTheMonthStrategy strategy = new TurnOfTheMonthStrategy(4, 3);
        // Jan 2 is <= 3 days after month start
        Candle candle = new Candle(
                LocalDateTime.of(2026, 1, 2, 9, 15),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(101),
                BigDecimal.valueOf(99),
                BigDecimal.valueOf(100),
                1000
        );
        MarketData data = new MarketData("TEST", List.of(candle));

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates BUY during last days of month")
    void testBuyLastDaysOfMonth() {
        TurnOfTheMonthStrategy strategy = new TurnOfTheMonthStrategy(4, 3);
        // Jan 29 in a 31-day month: 29 > 31 - 4 (27)
        Candle candle = new Candle(
                LocalDateTime.of(2026, 1, 29, 9, 15),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(101),
                BigDecimal.valueOf(99),
                BigDecimal.valueOf(100),
                1000
        );
        MarketData data = new MarketData("TEST", List.of(candle));

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL in the middle of the month")
    void testSellMidMonth() {
        TurnOfTheMonthStrategy strategy = new TurnOfTheMonthStrategy(4, 3);
        // Jan 15 is neither start nor end
        Candle candle = new Candle(
                LocalDateTime.of(2026, 1, 15, 9, 15),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(101),
                BigDecimal.valueOf(99),
                BigDecimal.valueOf(100),
                1000
        );
        MarketData data = new MarketData("TEST", List.of(candle));

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.SELL);
    }
}
