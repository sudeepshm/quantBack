package com.quantback.strategy;

import com.quantback.data.MarketData;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Quantpedia #11: Consistent Momentum Strategy
 * Based on Grinblatt and Moskowitz (2004): "Predicting Stock Price Movements From Past Returns:
 * The Role of Consistency and Tax-Loss Selling".
 *
 * Rule:
 * - Rather than just looking at total past return (which could be driven by a single outlier month),
 *   consistent momentum requires the asset to show positive returns across most sub-periods (buckets).
 * - Divides lookback period into `numBuckets` contiguous segments.
 * - Counts how many buckets had positive returns.
 * - BUY when (positiveBuckets / numBuckets) >= minConsistencyRatio (e.g. >= 0.67).
 * - SELL when (positiveBuckets / numBuckets) < 0.50 (momentum has broken down).
 * - HOLD otherwise or when data is insufficient.
 */
public class ConsistentMomentumStrategy implements Strategy {

    private final int lookbackBars;
    private final int numBuckets;
    private final double minConsistencyRatio;

    public ConsistentMomentumStrategy(int lookbackBars, int numBuckets, double minConsistencyRatio) {
        if (lookbackBars <= 0) {
            throw new IllegalArgumentException("Lookback bars must be positive: " + lookbackBars);
        }
        if (numBuckets <= 1) {
            throw new IllegalArgumentException("Number of buckets must be greater than 1: " + numBuckets);
        }
        if (lookbackBars < numBuckets) {
            throw new IllegalArgumentException("Lookback bars must be greater than or equal to numBuckets");
        }
        if (minConsistencyRatio <= 0.0 || minConsistencyRatio > 1.0) {
            throw new IllegalArgumentException("Consistency ratio must be between 0.0 and 1.0: " + minConsistencyRatio);
        }
        this.lookbackBars = lookbackBars;
        this.numBuckets = numBuckets;
        this.minConsistencyRatio = minConsistencyRatio;
    }

    public int getLookbackBars() {
        return lookbackBars;
    }

    public int getNumBuckets() {
        return numBuckets;
    }

    public double getMinConsistencyRatio() {
        return minConsistencyRatio;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        if (historicalData.size() <= lookbackBars) {
            return Signal.HOLD;
        }

        int currentIdx = historicalData.size() - 1;
        int bucketSize = lookbackBars / numBuckets;
        int positiveCount = 0;

        for (int b = 0; b < numBuckets; b++) {
            int end = currentIdx - (b * bucketSize);
            int start = end - bucketSize;
            if (start < 0) break;

            BigDecimal startPrice = historicalData.get(start).getClose();
            BigDecimal endPrice = historicalData.get(end).getClose();

            if (endPrice.compareTo(startPrice) > 0) {
                positiveCount++;
            }
        }

        double ratio = (double) positiveCount / numBuckets;

        if (ratio >= minConsistencyRatio) {
            return Signal.BUY;
        } else if (ratio < 0.50) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    @Override
    public String getName() {
        return String.format("ConsistentMomentum(%db, %d buckets, %.0f%%)", lookbackBars, numBuckets, minConsistencyRatio * 100);
    }
}
