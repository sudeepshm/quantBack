package com.quantback.strategy;

import com.quantback.data.Candle;
import com.quantback.data.MarketData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JanuaryBarometerStrategyTest {

    @Test
    @DisplayName("Returns HOLD during January itself")
    void testHoldDuringJanuary() {
        JanuaryBarometerStrategy strategy = new JanuaryBarometerStrategy();
        Candle c1 = new Candle(LocalDateTime.of(2026, 1, 15, 9, 15), BigDecimal.valueOf(100), BigDecimal.valueOf(101), BigDecimal.valueOf(99), BigDecimal.valueOf(100), 1000);
        MarketData data = new MarketData("TEST", List.of(c1));

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.HOLD);
    }

    @Test
    @DisplayName("Generates BUY in February if January return was positive")
    void testBuyInFebruaryOnPositiveJanuary() {
        JanuaryBarometerStrategy strategy = new JanuaryBarometerStrategy();
        // January: Open 100, Close 110 (+10% gain)
        Candle c1 = new Candle(LocalDateTime.of(2026, 1, 2, 9, 15), BigDecimal.valueOf(100), BigDecimal.valueOf(102), BigDecimal.valueOf(99), BigDecimal.valueOf(105), 1000);
        Candle c2 = new Candle(LocalDateTime.of(2026, 1, 30, 9, 15), BigDecimal.valueOf(106), BigDecimal.valueOf(112), BigDecimal.valueOf(105), BigDecimal.valueOf(110), 1000);
        // February 5 candle
        Candle c3 = new Candle(LocalDateTime.of(2026, 2, 5, 9, 15), BigDecimal.valueOf(110), BigDecimal.valueOf(112), BigDecimal.valueOf(109), BigDecimal.valueOf(111), 1000);

        MarketData data = new MarketData("TEST", List.of(c1, c2, c3));

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.BUY);
    }

    @Test
    @DisplayName("Generates SELL in February if January return was negative")
    void testSellInFebruaryOnNegativeJanuary() {
        JanuaryBarometerStrategy strategy = new JanuaryBarometerStrategy();
        // January: Open 100, Close 90 (-10% loss)
        Candle c1 = new Candle(LocalDateTime.of(2026, 1, 2, 9, 15), BigDecimal.valueOf(100), BigDecimal.valueOf(102), BigDecimal.valueOf(99), BigDecimal.valueOf(98), 1000);
        Candle c2 = new Candle(LocalDateTime.of(2026, 1, 30, 9, 15), BigDecimal.valueOf(95), BigDecimal.valueOf(96), BigDecimal.valueOf(89), BigDecimal.valueOf(90), 1000);
        // February 5 candle
        Candle c3 = new Candle(LocalDateTime.of(2026, 2, 5, 9, 15), BigDecimal.valueOf(90), BigDecimal.valueOf(92), BigDecimal.valueOf(88), BigDecimal.valueOf(89), 1000);

        MarketData data = new MarketData("TEST", List.of(c1, c2, c3));

        assertThat(strategy.generateSignal(data)).isEqualTo(Signal.SELL);
    }
}
