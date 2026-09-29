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

class HalloweenEffectStrategyTest {

    private Candle makeCandle(int year, int month, int day) {
        return new Candle(
                LocalDateTime.of(year, month, day, 10, 0),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(105),
                BigDecimal.valueOf(95),
                BigDecimal.valueOf(102),
                1000
        );
    }

    @Test
    @DisplayName("Validates constructor parameters")
    void testConstructorValidation() {
        assertThatThrownBy(() -> new HalloweenEffectStrategy(0, 5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HalloweenEffectStrategy(11, 13))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HalloweenEffectStrategy(5, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when data is empty")
    void testEmptyData() {
        HalloweenEffectStrategy strategy = new HalloweenEffectStrategy();
        MarketData data = new MarketData("TEST", List.of());
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY during winter months (November through April)")
    void testWinterMonthsGenerateBuy() {
        HalloweenEffectStrategy strategy = new HalloweenEffectStrategy(11, 5);

        // November
        MarketData nov = new MarketData("TEST", List.of(makeCandle(2026, 11, 15)));
        assertThat(strategy.generateSignal(nov)).isEqualTo(Signal.BUY);

        // December
        MarketData dec = new MarketData("TEST", List.of(makeCandle(2026, 12, 10)));
        assertThat(strategy.generateSignal(dec)).isEqualTo(Signal.BUY);

        // January
        MarketData jan = new MarketData("TEST", List.of(makeCandle(2026, 1, 5)));
        assertThat(strategy.generateSignal(jan)).isEqualTo(Signal.BUY);

        // April
        MarketData apr = new MarketData("TEST", List.of(makeCandle(2026, 4, 28)));
        assertThat(strategy.generateSignal(apr)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL during summer months (May through October)")
    void testSummerMonthsGenerateSell() {
        HalloweenEffectStrategy strategy = new HalloweenEffectStrategy(11, 5);

        // May
        MarketData may = new MarketData("TEST", List.of(makeCandle(2026, 5, 2)));
        assertThat(strategy.generateSignal(may)).isEqualTo(Signal.SELL);

        // July
        MarketData jul = new MarketData("TEST", List.of(makeCandle(2026, 7, 20)));
        assertThat(strategy.generateSignal(jul)).isEqualTo(Signal.SELL);

        // October
        MarketData oct = new MarketData("TEST", List.of(makeCandle(2026, 10, 31)));
        assertThat(strategy.generateSignal(oct)).isEqualTo(Signal.SELL);
    }
}
