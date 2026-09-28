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

class ShortTermReversalStrategyTest {

    @Test
    @DisplayName("Validates constructor parameters")
    void testConstructorValidation() {
        assertThatThrownBy(() -> new ShortTermReversalStrategy(1, 30.0, 70.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ShortTermReversalStrategy(5, 0.0, 70.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ShortTermReversalStrategy(5, 75.0, 70.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ShortTermReversalStrategy(5, 30.0, 100.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when data is insufficient for RSI")
    void testInsufficientData() {
        ShortTermReversalStrategy strategy = new ShortTermReversalStrategy(5, 30.0, 70.0);
        List<Candle> candles = createCandles(List.of(100.0, 101.0, 102.0)); // size 3 <= 5
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY when RSI drops into oversold territory (severe pullback)")
    void testBuyOnOversold() {
        ShortTermReversalStrategy strategy = new ShortTermReversalStrategy(3, 30.0, 70.0);
        // Consecutive sharp declines: 100 -> 90 -> 80 -> 70. All losses, avgGain = 0 -> RSI = 0 <= 30 => BUY
        List<Candle> candles = createCandles(List.of(100.0, 90.0, 80.0, 70.0));
        MarketData data = new MarketData("TEST", candles);

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL when RSI enters overbought territory")
    void testSellOnOverbought() {
        ShortTermReversalStrategy strategy = new ShortTermReversalStrategy(3, 30.0, 70.0);
        // Consecutive sharp gains: 100 -> 110 -> 120 -> 130. All gains, avgLoss = 0 -> RSI = 100 >= 70 => SELL
        List<Candle> candles = createCandles(List.of(100.0, 110.0, 120.0, 130.0));
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
