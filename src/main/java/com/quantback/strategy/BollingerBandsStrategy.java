package com.quantback.strategy;

import com.quantback.data.MarketData;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Quantpedia #22 & #63: Bollinger Bands Mean Reversion Strategy
 * Based on John Bollinger (2001): "Bollinger on Bollinger Bands" and
 * Leung and Chong (2003): "Evaluating the Performance of Bollinger Bands in Stock Markets".
 *
 * Rule:
 * - Calculates an N-period Simple Moving Average (SMA) of close prices and standard deviation bands:
 *   UpperBand = SMA + k * stdDev
 *   LowerBand = SMA - k * stdDev
 * - BUY when the closing price touches or breaches below the Lower Band (oversold mean-reversion opportunity).
 * - SELL when the closing price reaches or crosses above the Middle Band (SMA), locking in mean-reversion profit.
 * - HOLD when within normal bounds or when historical data is insufficient.
 */
public class BollingerBandsStrategy implements Strategy {

    private final int period;
    private final double stdDevMultiplier;
    private static final int SCALE = 6;
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    public BollingerBandsStrategy() {
        this(20, 2.0);
    }

    public BollingerBandsStrategy(int period, double stdDevMultiplier) {
        if (period <= 1) {
            throw new IllegalArgumentException("Period must be greater than 1: " + period);
        }
        if (stdDevMultiplier <= 0) {
            throw new IllegalArgumentException("Standard deviation multiplier must be positive: " + stdDevMultiplier);
        }
        this.period = period;
        this.stdDevMultiplier = stdDevMultiplier;
    }

    public int getPeriod() {
        return period;
    }

    public double getStdDevMultiplier() {
        return stdDevMultiplier;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        if (historicalData.size() < period) {
            return Signal.HOLD;
        }

        int currentIdx = historicalData.size() - 1;
        int startIdx = currentIdx - period + 1;

        // 1. Calculate SMA
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = startIdx; i <= currentIdx; i++) {
            sum = sum.add(historicalData.get(i).getClose());
        }
        BigDecimal sma = sum.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);

        // 2. Calculate Standard Deviation
        BigDecimal varianceSum = BigDecimal.ZERO;
        for (int i = startIdx; i <= currentIdx; i++) {
            BigDecimal diff = historicalData.get(i).getClose().subtract(sma);
            varianceSum = varianceSum.add(diff.multiply(diff));
        }
        BigDecimal variance = varianceSum.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
        BigDecimal stdDev = variance.sqrt(MC);

        // 3. Compute Bands
        BigDecimal bandOffset = stdDev.multiply(BigDecimal.valueOf(stdDevMultiplier));
        BigDecimal lowerBand = sma.subtract(bandOffset);

        BigDecimal currentClose = historicalData.get(currentIdx).getClose();

        // 4. Mean-reversion signals
        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return Signal.HOLD;
        }

        if (currentClose.compareTo(lowerBand) <= 0) {
            return Signal.BUY;
        } else if (currentClose.compareTo(sma) >= 0) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    @Override
    public String getName() {
        return String.format("BollingerBands(%d, %.1f)", period, stdDevMultiplier);
    }
}
