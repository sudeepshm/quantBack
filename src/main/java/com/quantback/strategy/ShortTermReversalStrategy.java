package com.quantback.strategy;

import com.quantback.data.MarketData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Quantpedia #67: Short-Term Reversal Effect in Stocks
 * Based on Jegadeesh (1990): "Evidence of Predictable Behavior of Security Returns"
 * and Lehmann (1990): "Fads, Martingales, and Market Efficiency".
 *
 * Implements a short-term RSI mean-reversion model:
 * - BUY when short-term RSI <= oversoldThreshold (e.g., 30) - stock is excessively beaten down.
 * - SELL when short-term RSI >= overboughtThreshold (e.g., 70) - bounce has completed.
 * - HOLD when RSI is between thresholds or historical data is insufficient.
 */
public class ShortTermReversalStrategy implements Strategy {

    private final int period;
    private final double oversoldThreshold;
    private final double overboughtThreshold;
    private static final int SCALE = 6;

    public ShortTermReversalStrategy(int period, double oversoldThreshold, double overboughtThreshold) {
        if (period <= 1) {
            throw new IllegalArgumentException("RSI period must be greater than 1: " + period);
        }
        if (oversoldThreshold <= 0 || oversoldThreshold >= overboughtThreshold) {
            throw new IllegalArgumentException("Oversold threshold must be positive and strictly less than overbought: " + oversoldThreshold);
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

        // Need at least period + 1 bars to calculate price changes
        if (historicalData.size() <= period) {
            return Signal.HOLD;
        }

        BigDecimal rsi = calculateRsi(historicalData, period);

        if (rsi.compareTo(BigDecimal.valueOf(oversoldThreshold)) <= 0) {
            return Signal.BUY;
        } else if (rsi.compareTo(BigDecimal.valueOf(overboughtThreshold)) >= 0) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    private BigDecimal calculateRsi(MarketData data, int window) {
        int currentIdx = data.size() - 1;
        int startIdx = currentIdx - window + 1;

        BigDecimal totalGain = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;

        for (int i = startIdx; i <= currentIdx; i++) {
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
        // RSI = 100 - (100 / (1 + RS))
        BigDecimal denominator = BigDecimal.ONE.add(rs);
        BigDecimal rsi = BigDecimal.valueOf(100).subtract(
                BigDecimal.valueOf(100).divide(denominator, SCALE, RoundingMode.HALF_UP)
        );

        return rsi;
    }

    @Override
    public String getName() {
        return String.format("ShortTermReversal(%d, %.0f, %.0f)", period, oversoldThreshold, overboughtThreshold);
    }
}
