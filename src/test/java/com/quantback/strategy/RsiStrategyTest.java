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

class RsiStrategyTest {

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
        assertThatThrownBy(() -> new RsiStrategy(1, 30, 70))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RsiStrategy(14, 0, 70))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RsiStrategy(14, 70, 70))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RsiStrategy(14, 30, 100))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when historical data has fewer bars than required")
    void testInsufficientData() {
        RsiStrategy strategy = new RsiStrategy(5, 30, 70);
        // Needs at least period + 2 = 7 bars
        List<Candle> candles = createCandles(List.of(100.0, 100.0, 100.0, 100.0, 100.0, 100.0));
        MarketData data = new MarketData("TEST", candles);
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY on oversold dip or recovery")
    void testBuyOnOversold() {
        RsiStrategy strategy = new RsiStrategy(5, 30, 70);
        // Continuous decline produces 0 gain and all losses, pushing RSI to 0 (<= 30)
        List<Candle> candles = createCandles(List.of(100.0, 95.0, 90.0, 85.0, 80.0, 75.0, 70.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL on overbought peak")
    void testSellOnOverbought() {
        RsiStrategy strategy = new RsiStrategy(5, 30, 70);
        // Continuous rise produces all gains and 0 loss, pushing RSI to 100 (>= 70)
        List<Candle> candles = createCandles(List.of(100.0, 105.0, 110.0, 115.0, 120.0, 125.0, 130.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.SELL);
    }

    @Test
    @DisplayName("Returns HOLD when RSI is in neutral middle zone")
    void testHoldInNeutralZone() {
        RsiStrategy strategy = new RsiStrategy(5, 30, 70);
        // Oscillating flat prices: RSI will be around 50
        List<Candle> candles = createCandles(List.of(100.0, 101.0, 100.0, 101.0, 100.0, 101.0, 100.0, 101.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Verifies strategy display name")
    void testStrategyName() {
        RsiStrategy strategy = new RsiStrategy(14, 30, 70);
        assertThat(strategy.getName()).isEqualTo("RSI(14, 30, 70)");
    }
}
