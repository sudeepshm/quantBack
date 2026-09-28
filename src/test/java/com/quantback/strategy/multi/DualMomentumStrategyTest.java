package com.quantback.strategy.multi;

import com.quantback.data.Candle;
import com.quantback.data.MarketData;
import com.quantback.strategy.Signal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DualMomentumStrategyTest {

    @Test
    @DisplayName("Selects winning asset when positive momentum exists")
    void testRelativeMomentumWinner() {
        DualMomentumStrategy strategy = new DualMomentumStrategy(2);

        // US: 100 -> 105 -> 115 (+15%)
        MarketData us = createData("SPY", List.of(100.0, 105.0, 115.0));
        // International: 100 -> 101 -> 103 (+3%)
        MarketData efa = createData("EFA", List.of(100.0, 101.0, 103.0));

        Map<String, Signal> signals = strategy.generateSignals(Map.of("SPY", us, "EFA", efa), LocalDateTime.now());

        assertThat(signals.get("SPY")).isEqualTo(Signal.BUY);
        assertThat(signals.get("EFA")).isEqualTo(Signal.SELL);
    }

    @Test
    @DisplayName("Exits to cash when all assets exhibit negative momentum")
    void testAbsoluteMomentumHurdleFailed() {
        DualMomentumStrategy strategy = new DualMomentumStrategy(2);

        // Both down
        MarketData us = createData("SPY", List.of(100.0, 95.0, 90.0));
        MarketData efa = createData("EFA", List.of(100.0, 90.0, 85.0));

        Map<String, Signal> signals = strategy.generateSignals(Map.of("SPY", us, "EFA", efa), LocalDateTime.now());

        // All exit to cash
        assertThat(signals.get("SPY")).isEqualTo(Signal.SELL);
        assertThat(signals.get("EFA")).isEqualTo(Signal.SELL);
    }

    private MarketData createData(String symbol, List<Double> prices) {
        List<Candle> candles = new ArrayList<>();
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 9, 15);
        for (int i = 0; i < prices.size(); i++) {
            BigDecimal p = BigDecimal.valueOf(prices.get(i));
            candles.add(new Candle(time.plusDays(i), p, p.add(BigDecimal.ONE), p.subtract(BigDecimal.ONE), p, 1000));
        }
        return new MarketData(symbol, candles);
    }
}
