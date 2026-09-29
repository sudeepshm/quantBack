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

class BollingerBandsStrategyTest {

    private Candle makeCandle(int index, double close) {
        return new Candle(
                LocalDateTime.of(2026, 1, 1, 9, 15).plusDays(index),
                BigDecimal.valueOf(close),
                BigDecimal.valueOf(close + 1),
                BigDecimal.valueOf(close - 1),
                BigDecimal.valueOf(close),
                1000
        );
    }

    @Test
    @DisplayName("Validates constructor parameters")
    void testConstructorValidation() {
        assertThatThrownBy(() -> new BollingerBandsStrategy(1, 2.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BollingerBandsStrategy(20, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Returns HOLD when data has fewer bars than period")
    void testInsufficientData() {
        BollingerBandsStrategy strategy = new BollingerBandsStrategy(5, 2.0);
        List<Candle> candles = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            candles.add(makeCandle(i, 100));
        }
        MarketData data = new MarketData("TEST", candles);
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY when close drops below lower band")
    void testBuyOnOversoldDip() {
        BollingerBandsStrategy strategy = new BollingerBandsStrategy(5, 2.0);
        List<Candle> candles = new ArrayList<>();
        // 4 bars around 100
        candles.add(makeCandle(0, 100));
        candles.add(makeCandle(1, 100));
        candles.add(makeCandle(2, 100));
        candles.add(makeCandle(3, 100));
        // 5th bar has sharp crash down to 80 -> drops way below lower band
        candles.add(makeCandle(4, 80));

        MarketData data = new MarketData("TEST", candles);
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL when close is at or above SMA")
    void testSellWhenAtOrAboveSma() {
        BollingerBandsStrategy strategy = new BollingerBandsStrategy(5, 2.0);
        List<Candle> candles = new ArrayList<>();
        candles.add(makeCandle(0, 95));
        candles.add(makeCandle(1, 98));
        candles.add(makeCandle(2, 100));
        candles.add(makeCandle(3, 102));
        candles.add(makeCandle(4, 105)); // Current close = 105, well above SMA (100) and upper band (~107)

        MarketData data = new MarketData("TEST", candles);
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.SELL);
    }

    @Test
    @DisplayName("Returns HOLD when between lower band and SMA")
    void testHoldBetweenLowerBandAndSma() {
        BollingerBandsStrategy strategy = new BollingerBandsStrategy(5, 2.0);
        List<Candle> candles = new ArrayList<>();
        candles.add(makeCandle(0, 100));
        candles.add(makeCandle(1, 100));
        candles.add(makeCandle(2, 100));
        candles.add(makeCandle(3, 80)); // Adds volatility
        candles.add(makeCandle(4, 92)); // Close = 92, which is below SMA (~94.4) but above Lower Band (~78.4)

        MarketData data = new MarketData("TEST", candles);
        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }
}
