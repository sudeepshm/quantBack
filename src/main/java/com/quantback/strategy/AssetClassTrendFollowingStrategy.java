package com.quantback.strategy;

import com.quantback.data.MarketData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Quantpedia #5 & #77: Asset Class Trend-Following Strategy
 * Based on Mebane T. Faber (2007): "A Quantitative Approach to Tactical Asset Allocation".
 *
 * Rule:
 * - BUY when the current close price is strictly above the N-period Simple Moving Average (SMA).
 * - SELL when the current close price is strictly below the N-period SMA.
 * - HOLD when equal or insufficient historical data.
 */
public class AssetClassTrendFollowingStrategy implements Strategy {

    private final int period;
    private static final int SCALE = 6;

    public AssetClassTrendFollowingStrategy(int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("Trend-following period must be positive: " + period);
        }
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        if (historicalData.size() < period) {
            return Signal.HOLD;
        }

        int currentIdx = historicalData.size() - 1;
        BigDecimal currentClose = historicalData.get(currentIdx).getClose();
        BigDecimal sma = calculateSma(historicalData, currentIdx, period);

        int comparison = currentClose.compareTo(sma);
        if (comparison > 0) {
            return Signal.BUY;
        } else if (comparison < 0) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    private BigDecimal calculateSma(MarketData data, int endIdxInclusive, int window) {
        int startIdx = endIdxInclusive - window + 1;
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = startIdx; i <= endIdxInclusive; i++) {
            sum = sum.add(data.get(i).getClose());
        }
        return sum.divide(BigDecimal.valueOf(window), SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public String getName() {
        return String.format("AssetClassTrendFollowing(%d)", period);
    }
}
