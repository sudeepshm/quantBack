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

class AssetClassTrendFollowingStrategyTest {

    @Test
    @DisplayName("Validates period is positive")
    void testConstructorValidation() {
        assertThatThrownBy(() -> new AssetClassTrendFollowingStrategy(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AssetClassTrendFollowingStrategy(-5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when data is shorter than period")
    void testInsufficientData() {
        AssetClassTrendFollowingStrategy strategy = new AssetClassTrendFollowingStrategy(5);
        List<Candle> candles = createCandles(List.of(100.0, 105.0, 110.0, 115.0)); // size 4 < 5
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY when current price closes above SMA")
    void testBuyAboveSma() {
        AssetClassTrendFollowingStrategy strategy = new AssetClassTrendFollowingStrategy(3);
        // SMA of 3 bars: (100 + 100 + 106) / 3 = 102. Current close is 106 > 102
        List<Candle> candles = createCandles(List.of(100.0, 100.0, 106.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL when current price closes below SMA")
    void testSellBelowSma() {
        AssetClassTrendFollowingStrategy strategy = new AssetClassTrendFollowingStrategy(3);
        // SMA of 3 bars: (100 + 100 + 94) / 3 = 98. Current close is 94 < 98
        List<Candle> candles = createCandles(List.of(100.0, 100.0, 94.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.SELL);
    }

    @Test
    @DisplayName("Generates HOLD when current price is exactly equal to SMA")
    void testHoldWhenEqual() {
        AssetClassTrendFollowingStrategy strategy = new AssetClassTrendFollowingStrategy(3);
        // SMA of 3 bars: (100 + 100 + 100) / 3 = 100. Current close is 100
        List<Candle> candles = createCandles(List.of(100.0, 100.0, 100.0));
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
