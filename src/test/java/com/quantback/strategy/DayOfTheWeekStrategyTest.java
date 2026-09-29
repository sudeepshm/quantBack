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

class DayOfTheWeekStrategyTest {

    private Candle makeCandle(LocalDateTime ldt) {
        return new Candle(
                ldt,
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
        assertThatThrownBy(() -> new DayOfTheWeekStrategy(0, 5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DayOfTheWeekStrategy(2, 8))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DayOfTheWeekStrategy(5, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when data is empty")
    void testEmptyData() {
        DayOfTheWeekStrategy strategy = new DayOfTheWeekStrategy();
        MarketData data = new MarketData("TEST", List.of());
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY on Tuesday through Friday")
    void testFavorableDaysGenerateBuy() {
        DayOfTheWeekStrategy strategy = new DayOfTheWeekStrategy(); // Default Tuesday (2) - Friday (5)

        // 2026-09-29 is a Tuesday
        Candle tue = makeCandle(LocalDateTime.of(2026, 9, 29, 10, 0));
        assertThat(strategy.generateSignal(new MarketData("TEST", List.of(tue)))).isEqualTo(Signal.BUY);

        // 2026-09-30 is Wednesday
        Candle wed = makeCandle(LocalDateTime.of(2026, 9, 30, 10, 0));
        assertThat(strategy.generateSignal(new MarketData("TEST", List.of(wed)))).isEqualTo(Signal.BUY);

        // 2026-10-02 is Friday
        Candle fri = makeCandle(LocalDateTime.of(2026, 10, 2, 10, 0));
        assertThat(strategy.generateSignal(new MarketData("TEST", List.of(fri)))).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL on Monday")
    void testMondayGeneratesSell() {
        DayOfTheWeekStrategy strategy = new DayOfTheWeekStrategy();

        // 2026-09-28 is a Monday
        Candle mon = makeCandle(LocalDateTime.of(2026, 9, 28, 10, 0));
        assertThat(strategy.generateSignal(new MarketData("TEST", List.of(mon)))).isEqualTo(Signal.SELL);
    }
}
