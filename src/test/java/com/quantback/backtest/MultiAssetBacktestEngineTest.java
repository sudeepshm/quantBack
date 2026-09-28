package com.quantback.backtest;

import com.quantback.data.Candle;
import com.quantback.data.MarketData;
import com.quantback.strategy.multi.DualMomentumStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MultiAssetBacktestEngineTest {

    @Test
    @DisplayName("Executes multi-asset simulation chronologically and generates portfolio metrics")
    void testMultiAssetSimulation() {
        MultiAssetBacktestEngine engine = new MultiAssetBacktestEngine();
        DualMomentumStrategy strategy = new DualMomentumStrategy(2);

        MarketData assetA = createData("ASSET_A", List.of(100.0, 105.0, 110.0, 120.0));
        MarketData assetB = createData("ASSET_B", List.of(100.0, 101.0, 102.0, 103.0));

        MultiAssetBacktestRequest request = new MultiAssetBacktestRequest(
                List.of("ASSET_A", "ASSET_B"),
                strategy,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                BigDecimal.valueOf(0.10),
                10,
                null,
                null
        );

        BacktestResult result = engine.run(Map.of("ASSET_A", assetA, "ASSET_B", assetB), request);

        assertThat(result).isNotNull();
        assertThat(result.getMetrics()).isNotNull();
        assertThat(result.getPortfolio().getSnapshots()).isNotEmpty();
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
