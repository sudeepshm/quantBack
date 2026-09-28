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

class PairsTradingStrategyTest {

    @Test
    @DisplayName("Generates BUY B and SELL A when ratio Z-score is high (A overvalued)")
    void testSpreadDivergenceHigh() {
        PairsTradingStrategy strategy = new PairsTradingStrategy("STOCK_A", "STOCK_B", 4, 1.5, 0.5);

        // Normally ratio is ~1.0: (100/100, 100/100, 100/100)
        // Last bar: A spikes to 150, B stays 100 -> ratio jumps to 1.5 -> large positive z-score
        MarketData dataA = createData("STOCK_A", List.of(100.0, 100.0, 100.0, 150.0));
        MarketData dataB = createData("STOCK_B", List.of(100.0, 100.0, 100.0, 100.0));

        Map<String, Signal> signals = strategy.generateSignals(
                Map.of("STOCK_A", dataA, "STOCK_B", dataB),
                LocalDateTime.now()
        );

        // A is overvalued -> SELL A. B is undervalued -> BUY B
        assertThat(signals.get("STOCK_A")).isEqualTo(Signal.SELL);
        assertThat(signals.get("STOCK_B")).isEqualTo(Signal.BUY);
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
