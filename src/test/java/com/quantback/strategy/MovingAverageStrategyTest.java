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

class MovingAverageStrategyTest {

    @Test
    @DisplayName("Validates fast and slow periods on construction")
    void testConstructorValidation() {
        assertThatThrownBy(() -> new MovingAverageStrategy(0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MovingAverageStrategy(10, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MovingAverageStrategy(10, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MovingAverageStrategy(15, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when data size is insufficient")
    void testInsufficientData() {
        MovingAverageStrategy strategy = new MovingAverageStrategy(2, 4);
        List<Candle> candles = createCandles(List.of(10.0, 10.0, 10.0, 10.0)); // size 4 <= slowPeriod 4
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY signal on golden cross")
    void testGoldenCross() {
        MovingAverageStrategy strategy = new MovingAverageStrategy(2, 3);
        // We need 4 candles (slowPeriod + 1):
        // Prices: 10, 10, 10, 20
        // At index 2: fast(2) = avg(10, 10) = 10, slow(3) = avg(10, 10, 10) = 10 -> fast <= slow
        // At index 3: fast(2) = avg(10, 20) = 15, slow(3) = avg(10, 10, 20) = 13.33 -> fast > slow
        List<Candle> candles = createCandles(List.of(10.0, 10.0, 10.0, 20.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL signal on death cross")
    void testDeathCross() {
        MovingAverageStrategy strategy = new MovingAverageStrategy(2, 3);
        // Prices: 20, 20, 20, 5
        // At index 2: fast(2) = 20, slow(3) = 20 -> fast >= slow
        // At index 3: fast(2) = avg(20, 5) = 12.5, slow(3) = avg(20, 20, 5) = 15 -> fast < slow
        List<Candle> candles = createCandles(List.of(20.0, 20.0, 20.0, 5.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.SELL);
    }

    @Test
    @DisplayName("Generates HOLD signal when no crossover occurs")
    void testNoCross() {
        MovingAverageStrategy strategy = new MovingAverageStrategy(2, 3);
        // Flat prices: 10, 10, 10, 10
        List<Candle> candles = createCandles(List.of(10.0, 10.0, 10.0, 10.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    private List<Candle> createCandles(List<Double> closePrices) {
        List<Candle> candles = new ArrayList<>();
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 9, 15);
        for (int i = 0; i < closePrices.size(); i++) {
            BigDecimal price = BigDecimal.valueOf(closePrices.get(i));
            candles.add(new Candle(
                    time.plusDays(i),
                    price,
                    price.add(BigDecimal.ONE),
                    price.subtract(BigDecimal.ONE).max(BigDecimal.valueOf(0.01)),
                    price,
                    1000
            ));
        }
        return candles;
    }
}
