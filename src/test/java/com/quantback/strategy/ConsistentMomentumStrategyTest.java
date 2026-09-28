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

class ConsistentMomentumStrategyTest {

    @Test
    @DisplayName("Validates constructor parameters")
    void testConstructorValidation() {
        assertThatThrownBy(() -> new ConsistentMomentumStrategy(0, 4, 0.75))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConsistentMomentumStrategy(20, 1, 0.75))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConsistentMomentumStrategy(10, 20, 0.75))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConsistentMomentumStrategy(20, 4, 1.5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when data is insufficient")
    void testInsufficientData() {
        ConsistentMomentumStrategy strategy = new ConsistentMomentumStrategy(6, 3, 0.67);
        List<Candle> candles = createCandles(List.of(10.0, 11.0, 12.0, 13.0, 14.0, 15.0)); // size 6 <= lookback 6
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY when consistent positive sub-periods satisfy threshold")
    void testBuyOnConsistentRally() {
        ConsistentMomentumStrategy strategy = new ConsistentMomentumStrategy(6, 3, 0.67);
        // Lookback 6, 3 buckets of 2 bars. All rising: 10, 11, 12, 13, 14, 15, 16
        List<Candle> candles = createCandles(List.of(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL when positive sub-periods fall below 50%")
    void testSellOnBreakdown() {
        ConsistentMomentumStrategy strategy = new ConsistentMomentumStrategy(6, 3, 0.67);
        // Lookback 6, 3 buckets of 2 bars. Downward: 20, 19, 18, 17, 16, 15, 14
        List<Candle> candles = createCandles(List.of(20.0, 19.0, 18.0, 17.0, 16.0, 15.0, 14.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.SELL);
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
