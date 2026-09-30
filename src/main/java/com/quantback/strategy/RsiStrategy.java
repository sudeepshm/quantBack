package com.quantback.strategy;

import com.quantback.data.MarketData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Quantpedia #68: Relative Strength Index (RSI) Overbought-Oversold & Reversion
 * Based on J. Welles Wilder Jr. (1978): "New Concepts in Technical Trading Systems".
 *
 * Rules:
 * - Computes Wilder's smoothed Relative Strength Index (RSI) over an N-period lookback (default 14).
 * - BUY when RSI is oversold (<= 30) or rebounds back above the oversold boundary.
 * - SELL when RSI is overbought (>= 70) or drops back below the overbought boundary.
 * - HOLD when RSI is within normal boundaries or historical data is insufficient.
 */
public class RsiStrategy implements Strategy {

    private final int period;
    private final double oversoldThreshold;
    private final double overboughtThreshold;

    private static final int SCALE = 6;

    public RsiStrategy() {
        this(14, 30.0, 70.0);
    }

    public RsiStrategy(int period, double oversoldThreshold, double overboughtThreshold) {
        if (period <= 1) {
            throw new IllegalArgumentException("RSI period must be greater than 1: " + period);
        }
        if (oversoldThreshold <= 0 || oversoldThreshold >= overboughtThreshold) {
            throw new IllegalArgumentException("Oversold threshold must be positive and less than overbought: " + oversoldThreshold);
        }
        if (overboughtThreshold >= 100) {
            throw new IllegalArgumentException("Overbought threshold must be less than 100: " + overboughtThreshold);
        }
        this.period = period;
        this.oversoldThreshold = oversoldThreshold;
        this.overboughtThreshold = overboughtThreshold;
    }

    public int getPeriod() {
        return period;
    }

    public double getOversoldThreshold() {
        return oversoldThreshold;
    }

    public double getOverboughtThreshold() {
        return overboughtThreshold;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        // Need at least period + 2 bars to evaluate previous and current RSI
        if (historicalData.size() <= period + 1) {
            return Signal.HOLD;
        }

        int currentIdx = historicalData.size() - 1;
        int prevIdx = currentIdx - 1;

        BigDecimal currentRsi = calculateWilderRsi(historicalData, currentIdx, period);
        BigDecimal prevRsi = calculateWilderRsi(historicalData, prevIdx, period);

        BigDecimal oversold = BigDecimal.valueOf(oversoldThreshold);
        BigDecimal overbought = BigDecimal.valueOf(overboughtThreshold);

        boolean isBuy = currentRsi.compareTo(oversold) <= 0
                || (prevRsi.compareTo(oversold) <= 0 && currentRsi.compareTo(oversold) > 0);

        boolean isSell = currentRsi.compareTo(overbought) >= 0
                || (prevRsi.compareTo(overbought) >= 0 && currentRsi.compareTo(overbought) < 0);

        if (isBuy) {
            return Signal.BUY;
        } else if (isSell) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    /**
     * Calculates Wilder's smoothed RSI ending at endIdx.
     */
    private BigDecimal calculateWilderRsi(MarketData data, int endIdx, int window) {
        int startIdx = endIdx - window + 1;

        BigDecimal totalGain = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;

        for (int i = startIdx; i <= endIdx; i++) {
            BigDecimal change = data.get(i).getClose().subtract(data.get(i - 1).getClose());
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                totalGain = totalGain.add(change);
            } else if (change.compareTo(BigDecimal.ZERO) < 0) {
                totalLoss = totalLoss.add(change.abs());
            }
        }

        BigDecimal avgGain = totalGain.divide(BigDecimal.valueOf(window), SCALE, RoundingMode.HALF_UP);
        BigDecimal avgLoss = totalLoss.divide(BigDecimal.valueOf(window), SCALE, RoundingMode.HALF_UP);

        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        if (avgGain.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal rs = avgGain.divide(avgLoss, SCALE, RoundingMode.HALF_UP);
        BigDecimal denominator = BigDecimal.ONE.add(rs);
        return BigDecimal.valueOf(100).subtract(
                BigDecimal.valueOf(100).divide(denominator, SCALE, RoundingMode.HALF_UP)
        );
    }

    @Override
    public String getName() {
        return String.format("RSI(%d, %.0f, %.0f)", period, oversoldThreshold, overboughtThreshold);
    }
}
