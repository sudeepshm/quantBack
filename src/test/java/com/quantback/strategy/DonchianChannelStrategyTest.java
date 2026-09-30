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

class DonchianChannelStrategyTest {

    private Candle makeCandle(int index, double open, double high, double low, double close) {
        return new Candle(
                LocalDateTime.of(2026, 1, 1, 9, 15).plusDays(index),
                BigDecimal.valueOf(open),
                BigDecimal.valueOf(high),
                BigDecimal.valueOf(low),
                BigDecimal.valueOf(close),
                1000
        );
    }

    @Test
    @DisplayName("Validates constructor parameters")
    void testConstructorValidation() {
        assertThatThrownBy(() -> new DonchianChannelStrategy(1, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DonchianChannelStrategy(20, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when historical data has fewer bars than required")
    void testInsufficientData() {
        DonchianChannelStrategy strategy = new DonchianChannelStrategy(5, 3);
        List<Candle> candles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            candles.add(makeCandle(i, 100, 105, 95, 100));
        }
        MarketData data = new MarketData("TEST", candles);
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY when current close breaks above previous high channel")
    void testBuyOnUpperChannelBreakout() {
        DonchianChannelStrategy strategy = new DonchianChannelStrategy(5, 3);
        List<Candle> candles = new ArrayList<>();
        // 5 prior bars with highs <= 110
        for (int i = 0; i < 5; i++) {
            candles.add(makeCandle(i, 100, 110, 95, 102));
        }
        // 6th bar closes at 115, exceeding prior 5-bar high of 110
        candles.add(makeCandle(5, 110, 116, 109, 115));

        MarketData data = new MarketData("TEST", candles);
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL when current close breaks below previous low exit channel")
    void testSellOnLowerExitChannelBreakdown() {
        DonchianChannelStrategy strategy = new DonchianChannelStrategy(5, 3);
        List<Candle> candles = new ArrayList<>();
        // 5 prior bars with lows around 90 (last 3 bars lows are 90)
        for (int i = 0; i < 5; i++) {
            candles.add(makeCandle(i, 100, 105, 90, 98));
        }
        // 6th bar closes at 85, breaking below exit low of 90
        candles.add(makeCandle(5, 92, 93, 84, 85));

        MarketData data = new MarketData("TEST", candles);
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.SELL);
    }

    @Test
    @DisplayName("Returns HOLD when price stays within channel bounds")
    void testHoldInsideChannel() {
        DonchianChannelStrategy strategy = new DonchianChannelStrategy(5, 3);
        List<Candle> candles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            candles.add(makeCandle(i, 100, 110, 90, 100));
        }
        // 6th bar close is 105 (between 90 and 110)
        candles.add(makeCandle(5, 100, 108, 95, 105));

        MarketData data = new MarketData("TEST", candles);
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Verifies strategy display name")
    void testStrategyName() {
        DonchianChannelStrategy strategy = new DonchianChannelStrategy(20, 10);
        assertThat(strategy.getName()).isEqualTo("DonchianChannel(20, 10)");
    }
}
