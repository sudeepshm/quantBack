package com.quantback.strategy;

import com.quantback.data.Candle;
import com.quantback.data.MarketData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MacdStrategyTest {

    private Candle makeCandle(int index, double close) {
        BigDecimal price = BigDecimal.valueOf(close);
        return new Candle(
                LocalDateTime.of(2026, 1, 1, 9, 15).plusDays(index),
                price,
                price.add(BigDecimal.ONE),
                price.subtract(BigDecimal.ONE).max(BigDecimal.valueOf(0.01)),
                price,
                1000
        );
    }

    private List<Candle> createCandles(List<Double> closes) {
        List<Candle> list = new ArrayList<>();
        for (int i = 0; i < closes.size(); i++) {
            list.add(makeCandle(i, closes.get(i)));
        }
        return list;
    }

    @Test
    @DisplayName("Validates constructor parameters")
    void testConstructorValidation() {
        assertThatThrownBy(() -> new MacdStrategy(0, 26, 9))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MacdStrategy(26, 26, 9))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MacdStrategy(12, 10, 9))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MacdStrategy(12, 26, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when historical data has fewer bars than required")
    void testInsufficientData() {
        MacdStrategy strategy = new MacdStrategy(2, 4, 2);
        // Needs at least 4 + 2 = 6 bars
        List<Candle> candles = createCandles(List.of(10.0, 10.0, 10.0, 10.0, 10.0));
        MarketData data = new MarketData("TEST", candles);
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY on bullish MACD crossover above signal line")
    void testBullishCrossover() {
        MacdStrategy strategy = new MacdStrategy(2, 4, 2);
        // 5 flat bars at 10.0, then sudden upward breakout to 30.0
        // At bar 4: MACD = 0, Signal = 0
        // At bar 5: Fast EMA jumps much higher than Slow EMA -> MACD > Signal
        List<Candle> candles = createCandles(List.of(10.0, 10.0, 10.0, 10.0, 10.0, 30.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL on bearish MACD crossover below signal line")
    void testBearishCrossover() {
        MacdStrategy strategy = new MacdStrategy(2, 4, 2);
        // 5 flat bars at 20.0, then sudden plunge to 5.0
        // At bar 4: MACD = 0, Signal = 0
        // At bar 5: Fast EMA plunges lower than Slow EMA -> MACD < Signal
        List<Candle> candles = createCandles(List.of(20.0, 20.0, 20.0, 20.0, 20.0, 5.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.SELL);
    }

    @Test
    @DisplayName("Returns HOLD when price is flat and no crossover occurs")
    void testHoldOnFlatPrices() {
        MacdStrategy strategy = new MacdStrategy(2, 4, 2);
        List<Candle> candles = createCandles(List.of(10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0));
        MarketData data = new MarketData("TEST", candles);
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Verifies strategy display name")
    void testStrategyName() {
        MacdStrategy strategy = new MacdStrategy(12, 26, 9);
        assertThat(strategy.getName()).isEqualTo("MACD(12, 26, 9)");
    }
}
