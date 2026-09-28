package com.quantback.strategy.multi;

import com.quantback.data.MarketData;
import com.quantback.strategy.Signal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Quantpedia #64: Sector Momentum - Rotational System
 * Based on Moskowitz and Grinblatt (1999): "Do Industries Explain Momentum?".
 *
 * Rule:
 * - Ranks universe of sector ETFs or stocks by their historical return over `lookbackBars` (e.g. 60 or 120 bars).
 * - Rotates capital into the top `topK` assets that have strictly positive momentum (BUY).
 * - Issues SELL for any asset that falls out of the top K or has negative momentum.
 */
public class SectorMomentumStrategy implements MultiAssetStrategy {

    private final int lookbackBars;
    private final int topK;
    private static final int SCALE = 6;

    public SectorMomentumStrategy(int lookbackBars, int topK) {
        if (lookbackBars <= 0) {
            throw new IllegalArgumentException("Lookback bars must be positive: " + lookbackBars);
        }
        if (topK <= 0) {
            throw new IllegalArgumentException("Top K must be positive: " + topK);
        }
        this.lookbackBars = lookbackBars;
        this.topK = topK;
    }

    public int getLookbackBars() {
        return lookbackBars;
    }

    public int getTopK() {
        return topK;
    }

    @Override
    public Map<String, Signal> generateSignals(Map<String, MarketData> universeData, LocalDateTime currentTimestamp) {
        Objects.requireNonNull(universeData, "Universe data cannot be null");

        Map<String, Signal> signals = new HashMap<>();
        List<AssetReturn> returns = new ArrayList<>();

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

            returns.add(new AssetReturn(symbol, ret));
        }

        // Sort descending by return
        returns.sort(Comparator.comparing(AssetReturn::ret).reversed());

        Set<String> selectedTopSymbols = new HashSet<>();
        for (int i = 0; i < Math.min(topK, returns.size()); i++) {
            AssetReturn ar = returns.get(i);
            // Absolute momentum filter: must be strictly positive return
            if (ar.ret().compareTo(BigDecimal.ZERO) > 0) {
                selectedTopSymbols.add(ar.symbol());
                signals.put(ar.symbol(), Signal.BUY);
            } else {
                signals.put(ar.symbol(), Signal.SELL);
            }
        }

        // Remaining assets get SELL
        for (int i = topK; i < returns.size(); i++) {
            signals.put(returns.get(i).symbol(), Signal.SELL);
        }

        return signals;
    }

    private record AssetReturn(String symbol, BigDecimal ret) {}

    @Override
    public String getName() {
        return String.format("SectorMomentum(%d bars, top %d)", lookbackBars, topK);
    }
}
