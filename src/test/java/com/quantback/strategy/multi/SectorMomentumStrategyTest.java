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

class SectorMomentumStrategyTest {

    @Test
    @DisplayName("Selects top K assets with positive return and sells lagging assets")
    void testSectorRotation() {
        SectorMomentumStrategy strategy = new SectorMomentumStrategy(2, 1); // lookback 2, top 1

        // Asset A: 100 -> 105 -> 120 (+20% return)
        MarketData assetA = createData("ASSET_A", List.of(100.0, 105.0, 120.0));
        // Asset B: 100 -> 102 -> 105 (+5% return)
        MarketData assetB = createData("ASSET_B", List.of(100.0, 102.0, 105.0));
        // Asset C: 100 -> 95 -> 90 (-10% return)
        MarketData assetC = createData("ASSET_C", List.of(100.0, 95.0, 90.0));

        Map<String, MarketData> universe = Map.of("ASSET_A", assetA, "ASSET_B", assetB, "ASSET_C", assetC);
        Map<String, Signal> signals = strategy.generateSignals(universe, LocalDateTime.now());

        // Asset A is top 1 with positive return -> BUY
        assertThat(signals.get("ASSET_A")).isEqualTo(Signal.BUY);
        // Asset B is not top 1 -> SELL
        assertThat(signals.get("ASSET_B")).isEqualTo(Signal.SELL);
        // Asset C is negative return -> SELL
        assertThat(signals.get("ASSET_C")).isEqualTo(Signal.SELL);
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
