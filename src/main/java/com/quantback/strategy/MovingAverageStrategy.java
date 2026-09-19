package com.quantback.strategy;

import com.quantback.data.MarketData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Moving Average Crossover Strategy.
 * Generates:
 * - BUY when Fast Moving Average crosses above Slow Moving Average
 * - SELL when Fast Moving Average crosses below Slow Moving Average
 * - HOLD otherwise or when there is insufficient historical data
 */
public class MovingAverageStrategy implements Strategy {

    private final int fastPeriod;
    private final int slowPeriod;
    private static final int SCALE = 6;

    public MovingAverageStrategy(int fastPeriod, int slowPeriod) {
        if (fastPeriod <= 0) {
            throw new IllegalArgumentException("Fast period must be positive: " + fastPeriod);
        }
        if (slowPeriod <= 0) {
            throw new IllegalArgumentException("Slow period must be positive: " + slowPeriod);
        }
        if (fastPeriod >= slowPeriod) {
            throw new IllegalArgumentException(
                    String.format("Fast period (%d) must be strictly less than slow period (%d)", fastPeriod, slowPeriod)
            );
        }
        this.fastPeriod = fastPeriod;
        this.slowPeriod = slowPeriod;
    }

    public int getFastPeriod() {
        return fastPeriod;
    }

    public int getSlowPeriod() {
        return slowPeriod;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        // Need at least slowPeriod + 1 candles to compute crossover (previous vs current)
        if (historicalData.size() <= slowPeriod) {
            return Signal.HOLD;
        }

        int currentIdx = historicalData.size() - 1;
        int prevIdx = currentIdx - 1;

        BigDecimal fastCurr = calculateSma(historicalData, currentIdx, fastPeriod);
        BigDecimal fastPrev = calculateSma(historicalData, prevIdx, fastPeriod);

        BigDecimal slowCurr = calculateSma(historicalData, currentIdx, slowPeriod);
        BigDecimal slowPrev = calculateSma(historicalData, prevIdx, slowPeriod);

        // Golden Cross: Fast MA crosses above Slow MA
        if (fastPrev.compareTo(slowPrev) <= 0 && fastCurr.compareTo(slowCurr) > 0) {
            return Signal.BUY;
        }

        // Death Cross: Fast MA crosses below Slow MA
        if (fastPrev.compareTo(slowPrev) >= 0 && fastCurr.compareTo(slowCurr) < 0) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    private BigDecimal calculateSma(MarketData data, int endIdxInclusive, int period) {
        int startIdx = endIdxInclusive - period + 1;
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = startIdx; i <= endIdxInclusive; i++) {
            sum = sum.add(data.get(i).getClose());
        }
        return sum.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public String getName() {
        return String.format("MovingAverageCrossover(%d, %d)", fastPeriod, slowPeriod);
    }
}
