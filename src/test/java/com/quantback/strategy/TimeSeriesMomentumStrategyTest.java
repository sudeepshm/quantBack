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

class TimeSeriesMomentumStrategyTest {

    @Test
    @DisplayName("Validates lookback period is positive")
    void testConstructorValidation() {
        assertThatThrownBy(() -> new TimeSeriesMomentumStrategy(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TimeSeriesMomentumStrategy(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when data size is not strictly greater than lookback")
    void testInsufficientData() {
        TimeSeriesMomentumStrategy strategy = new TimeSeriesMomentumStrategy(3);
        List<Candle> candles = createCandles(List.of(100.0, 105.0, 110.0)); // size 3 <= lookback 3
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY when current price is higher than price L periods ago")
    void testBuyOnPositiveMomentum() {
        TimeSeriesMomentumStrategy strategy = new TimeSeriesMomentumStrategy(2);
        // Past price at index (3 - 2) = 1 is 105.0. Current price at index 3 is 120.0 > 105.0
        List<Candle> candles = createCandles(List.of(100.0, 105.0, 110.0, 120.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL when current price is lower than price L periods ago")
    void testSellOnNegativeMomentum() {
        TimeSeriesMomentumStrategy strategy = new TimeSeriesMomentumStrategy(2);
        // Past price at index (3 - 2) = 1 is 115.0. Current price at index 3 is 95.0 < 115.0
        List<Candle> candles = createCandles(List.of(100.0, 115.0, 105.0, 95.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.SELL);
    }

    @Test
    @DisplayName("Generates HOLD when current price equals price L periods ago")
    void testHoldOnFlatMomentum() {
        TimeSeriesMomentumStrategy strategy = new TimeSeriesMomentumStrategy(2);
        // Past price at index 1 is 100.0, current price at index 3 is 100.0
        List<Candle> candles = createCandles(List.of(100.0, 100.0, 110.0, 100.0));
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
