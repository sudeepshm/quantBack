package com.quantback.strategy.multi;

import com.quantback.data.MarketData;
import com.quantback.strategy.Signal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Quantpedia #35: Momentum Asset Allocation Strategy (Dual Momentum)
 * Based on Gary Antonacci (2012): "Risk Premia Harvesting Through Dual Momentum"
 * and Mebane Faber (2013): "Global Tactical Asset Allocation".
 *
 * Rule:
 * 1. Relative Momentum: Rank candidate risky assets by their lookback return. Select the winner.
 * 2. Absolute Momentum: If the winner has a strictly positive return, BUY the winner and SELL the others.
 *    If the winner's return <= 0, SELL all assets (move to 100% cash safety).
 */
public class DualMomentumStrategy implements MultiAssetStrategy {

    private final int lookbackBars;
    private static final int SCALE = 6;

    public DualMomentumStrategy(int lookbackBars) {
        if (lookbackBars <= 0) {
            throw new IllegalArgumentException("Lookback bars must be positive: " + lookbackBars);
        }
        this.lookbackBars = lookbackBars;
    }

    public int getLookbackBars() {
        return lookbackBars;
    }

    @Override
    public Map<String, Signal> generateSignals(Map<String, MarketData> universeData, LocalDateTime currentTimestamp) {
        Objects.requireNonNull(universeData, "Universe data cannot be null");

        Map<String, Signal> signals = new HashMap<>();
        String bestSymbol = null;
        BigDecimal bestReturn = null;

        for (Map.Entry<String, MarketData> entry : universeData.entrySet()) {
            String symbol = entry.getKey();
            MarketData data = entry.getValue();

            if (data.size() <= lookbackBars) {
                signals.put(symbol, Signal.HOLD);
                continue;
            }

            int currentIdx = data.size() - 1;
            int pastIdx = currentIdx - lookbackBars;

            BigDecimal currentPrice = data.get(currentIdx).getClose();
            BigDecimal pastPrice = data.get(pastIdx).getClose();

            if (pastPrice.compareTo(BigDecimal.ZERO) <= 0) {
                signals.put(symbol, Signal.HOLD);
                continue;
            }

            BigDecimal ret = currentPrice.subtract(pastPrice)
                    .divide(pastPrice, SCALE, RoundingMode.HALF_UP);

            if (bestReturn == null || ret.compareTo(bestReturn) > 0) {
                bestReturn = ret;
                bestSymbol = symbol;
            }
        }

        // Absolute momentum check
        if (bestSymbol != null && bestReturn != null && bestReturn.compareTo(BigDecimal.ZERO) > 0) {
            for (String symbol : universeData.keySet()) {
                if (symbol.equals(bestSymbol)) {
                    signals.put(symbol, Signal.BUY);
                } else {
                    signals.put(symbol, Signal.SELL);
                }
            }
        } else {
            // All assets negative or insufficient -> exit all to cash
            for (String symbol : universeData.keySet()) {
                signals.put(symbol, Signal.SELL);
            }
        }

        return signals;
    }

    @Override
    public String getName() {
        return String.format("DualMomentum(%d bars)", lookbackBars);
    }
}
