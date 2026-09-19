package com.quantback.backtest;

import com.quantback.data.Candle;
import com.quantback.data.MarketData;
import com.quantback.strategy.MovingAverageStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BacktestEngineTest {

    @Test
    @DisplayName("Throws exception when market data is empty")
    void throwsOnEmptyMarketData() {
        BacktestEngine engine = new BacktestEngine();
        MarketData emptyData = new MarketData("NIFTY", Collections.emptyList());
        BacktestRequest request = new BacktestRequest(
                "NIFTY",
                new MovingAverageStrategy(2, 4),
                new BigDecimal("100000.00")
        );

        assertThatThrownBy(() -> engine.run(emptyData, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot run backtest on empty market data");
    }

    @Test
    @DisplayName("Runs full end-to-end backtest and generates complete BacktestResult")
    void testFullBacktestRun() {
        BacktestEngine engine = new BacktestEngine();

        // Create a trend: flat, then rising (triggers BUY), then falling (triggers SELL)
        List<Double> prices = List.of(
                100.0, 100.0, 100.0, 100.0, // indices 0-3: flat
                110.0, 120.0, 130.0,        // indices 4-6: rising (Golden Cross)
                115.0, 90.0, 80.0           // indices 7-9: falling (Death Cross)
        );

        List<Candle> candles = new ArrayList<>();
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 9, 15);
        for (int i = 0; i < prices.size(); i++) {
            BigDecimal p = BigDecimal.valueOf(prices.get(i));
            candles.add(new Candle(base.plusDays(i), p, p.add(BigDecimal.ONE), p.subtract(BigDecimal.ONE), p, 1000));
        }
        MarketData data = new MarketData("NIFTY", candles);

        BacktestRequest request = new BacktestRequest(
                "NIFTY",
                new MovingAverageStrategy(2, 3),
                new BigDecimal("50000.00"),
                new BigDecimal("0.10"), // slippage
                new BigDecimal("5.00"), // fee
                10,
                null,
                null
        );

        BacktestResult result = engine.run(data, request);

        assertThat(result).isNotNull();
        assertThat(result.getSnapshots()).hasSize(prices.size());
        assertThat(result.getTrades()).isNotEmpty();
        assertThat(result.getMetrics()).isNotNull();
        assertThat(result.getFinalCapital()).isNotNull();
    }
}
