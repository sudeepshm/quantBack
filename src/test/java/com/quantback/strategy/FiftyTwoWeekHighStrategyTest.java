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

class FiftyTwoWeekHighStrategyTest {

    @Test
    @DisplayName("Validates constructor parameters")
    void testConstructorValidation() {
        assertThatThrownBy(() -> new FiftyTwoWeekHighStrategy(0, 5.0, 10.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FiftyTwoWeekHighStrategy(200, -1.0, 10.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FiftyTwoWeekHighStrategy(200, 10.0, 5.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FiftyTwoWeekHighStrategy(200, 5.0, 100.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when data size is less than lookback")
    void testInsufficientData() {
        FiftyTwoWeekHighStrategy strategy = new FiftyTwoWeekHighStrategy(10, 5.0, 10.0);
        List<Candle> candles = createCandles(List.of(100.0, 105.0, 110.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY when current close is within threshold of highest high")
    void testBuyNearHigh() {
        FiftyTwoWeekHighStrategy strategy = new FiftyTwoWeekHighStrategy(5, 5.0, 10.0);
        // Lookback 5: highest high is 100.0 (high = close + 1 = 101.0).
        // Current close is 98.0, threshold 5% -> buyLevel = 101 * 0.95 = 95.95 -> 98 >= 95.95 => BUY
        List<Candle> candles = createCandles(List.of(90.0, 95.0, 100.0, 92.0, 94.0, 98.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL when current close drops below exit buffer")
    void testSellBelowBuffer() {
        FiftyTwoWeekHighStrategy strategy = new FiftyTwoWeekHighStrategy(5, 5.0, 10.0);
        // Lookback 5: past highs are around 101.0. Sell level is 101 * 0.90 = 90.9
        // Current close is 85.0 < 90.9 => SELL
        List<Candle> candles = createCandles(List.of(90.0, 95.0, 100.0, 92.0, 94.0, 85.0));
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
