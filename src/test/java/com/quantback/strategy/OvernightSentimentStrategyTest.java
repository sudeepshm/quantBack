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

class OvernightSentimentStrategyTest {

    @Test
    @DisplayName("Validates threshold is non-negative")
    void testConstructorValidation() {
        assertThatThrownBy(() -> new OvernightSentimentStrategy(-0.5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when less than 2 candles")
    void testInsufficientData() {
        OvernightSentimentStrategy strategy = new OvernightSentimentStrategy(0.1);
        Candle c1 = new Candle(LocalDateTime.of(2026, 1, 1, 9, 15), BigDecimal.valueOf(100), BigDecimal.valueOf(105), BigDecimal.valueOf(98), BigDecimal.valueOf(102), 1000);
        MarketData data = new MarketData("TEST", List.of(c1));

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY when overnight gap up exceeds threshold")
    void testBuyOnBullishGap() {
        OvernightSentimentStrategy strategy = new OvernightSentimentStrategy(0.5); // 0.5% threshold
        // Day 1 Close: 100. Day 2 Open: 101 -> +1.0% gap up >= 0.5%
        Candle c1 = new Candle(LocalDateTime.of(2026, 1, 1, 9, 15), BigDecimal.valueOf(99), BigDecimal.valueOf(101), BigDecimal.valueOf(98), BigDecimal.valueOf(100), 1000);
        Candle c2 = new Candle(LocalDateTime.of(2026, 1, 2, 9, 15), BigDecimal.valueOf(101), BigDecimal.valueOf(102), BigDecimal.valueOf(100), BigDecimal.valueOf(101.5), 1000);
        MarketData data = new MarketData("TEST", List.of(c1, c2));

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL when overnight gap down drops below negative threshold")
    void testSellOnBearishGap() {
        OvernightSentimentStrategy strategy = new OvernightSentimentStrategy(0.5);
        // Day 1 Close: 100. Day 2 Open: 99 -> -1.0% gap down <= -0.5%
        Candle c1 = new Candle(LocalDateTime.of(2026, 1, 1, 9, 15), BigDecimal.valueOf(99), BigDecimal.valueOf(101), BigDecimal.valueOf(98), BigDecimal.valueOf(100), 1000);
        Candle c2 = new Candle(LocalDateTime.of(2026, 1, 2, 9, 15), BigDecimal.valueOf(99), BigDecimal.valueOf(100), BigDecimal.valueOf(97), BigDecimal.valueOf(98), 1000);
        MarketData data = new MarketData("TEST", List.of(c1, c2));

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.SELL);
    }
}
