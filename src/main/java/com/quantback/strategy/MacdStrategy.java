package com.quantback.strategy;

import com.quantback.data.MarketData;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Quantpedia #73: Moving Average Convergence Divergence (MACD)
 * Based on Gerald Appel (1979): "Systems and Forecasts".
 *
 * Rules:
 * - Fast EMA (default 12) and Slow EMA (default 26) on closing prices.
 * - MACD Line = Fast EMA - Slow EMA.
 * - Signal Line = EMA of MACD Line (default 9-period).
 * - BUY when MACD Line crosses above the Signal Line (Bullish Golden Crossover).
 * - SELL when MACD Line crosses below the Signal Line (Bearish Death Crossover).
 * - HOLD when no crossover occurs or historical data is insufficient.
 */
public class MacdStrategy implements Strategy {

    private final int fastPeriod;
    private final int slowPeriod;
    private final int signalPeriod;

    private static final int SCALE = 6;
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    public MacdStrategy() {
        this(12, 26, 9);
    }

    public MacdStrategy(int fastPeriod, int slowPeriod, int signalPeriod) {
        if (fastPeriod < 1) {
            throw new IllegalArgumentException("Fast period must be at least 1: " + fastPeriod);
        }
        if (slowPeriod <= fastPeriod) {
            throw new IllegalArgumentException("Slow period (" + slowPeriod + ") must be greater than fast period (" + fastPeriod + ")");
        }
        if (signalPeriod < 1) {
            throw new IllegalArgumentException("Signal period must be at least 1: " + signalPeriod);
        }
        this.fastPeriod = fastPeriod;
        this.slowPeriod = slowPeriod;
        this.signalPeriod = signalPeriod;
    }

    public int getFastPeriod() {
        return fastPeriod;
    }

    public int getSlowPeriod() {
        return slowPeriod;
    }

    public int getSignalPeriod() {
        return signalPeriod;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        // Need enough candles for slow EMA, signal EMA, plus 1 prior bar for crossover detection
        int minRequired = slowPeriod + signalPeriod;
        if (historicalData.size() < minRequired) {
            return Signal.HOLD;
        }

        // 1. Extract closing prices
        int n = historicalData.size();
        List<BigDecimal> closes = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            closes.add(historicalData.get(i).getClose());
        }

        // 2. Compute Fast and Slow EMAs
        List<BigDecimal> fastEma = computeEma(closes, fastPeriod);
        List<BigDecimal> slowEma = computeEma(closes, slowPeriod);

        // 3. Compute MACD Line = Fast EMA - Slow EMA (valid from index slowPeriod - 1 onwards)
        List<BigDecimal> macdSeries = new ArrayList<>();
        for (int i = slowPeriod - 1; i < n; i++) {
            BigDecimal fast = fastEma.get(i);
            BigDecimal slow = slowEma.get(i);
            macdSeries.add(fast.subtract(slow, MC).setScale(SCALE, RoundingMode.HALF_UP));
        }

        if (macdSeries.size() < signalPeriod + 1) {
            return Signal.HOLD;
        }

        // 4. Compute Signal Line (EMA of MACD Line)
        List<BigDecimal> signalSeries = computeEma(macdSeries, signalPeriod);

        // 5. Crossover Detection at current and previous bars
        int lastIdx = macdSeries.size() - 1;
        int prevIdx = lastIdx - 1;

        BigDecimal currentMacd = macdSeries.get(lastIdx);
        BigDecimal currentSignal = signalSeries.get(lastIdx);
        BigDecimal prevMacd = macdSeries.get(prevIdx);
        BigDecimal prevSignal = signalSeries.get(prevIdx);

        boolean bullishCrossover = prevMacd.compareTo(prevSignal) <= 0 && currentMacd.compareTo(currentSignal) > 0;
        boolean bearishCrossover = prevMacd.compareTo(prevSignal) >= 0 && currentMacd.compareTo(currentSignal) < 0;

        if (bullishCrossover) {
            return Signal.BUY;
        } else if (bearishCrossover) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    /**
     * Computes an Exponential Moving Average (EMA) over a series of values.
     * The first EMA value at index (period - 1) is initialized with the Simple Moving Average.
     */
    private List<BigDecimal> computeEma(List<BigDecimal> values, int period) {
        int n = values.size();
        List<BigDecimal> ema = new ArrayList<>(n);

        // Pad initial elements with ZERO before period is reached
        for (int i = 0; i < period - 1; i++) {
            ema.add(BigDecimal.ZERO);
        }

        // Initial SMA
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < period; i++) {
            sum = sum.add(values.get(i));
        }
        BigDecimal currentEma = sum.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
        ema.add(currentEma);

        // Multiplier alpha = 2 / (period + 1)
        BigDecimal alpha = BigDecimal.valueOf(2.0).divide(BigDecimal.valueOf(period + 1.0), MC);
        BigDecimal oneMinusAlpha = BigDecimal.ONE.subtract(alpha);

        // Successive EMA calculations
        for (int i = period; i < n; i++) {
            BigDecimal price = values.get(i);
            currentEma = price.multiply(alpha, MC).add(currentEma.multiply(oneMinusAlpha, MC)).setScale(SCALE, RoundingMode.HALF_UP);
            ema.add(currentEma);
        }

        return ema;
    }

    @Override
    public String getName() {
        return String.format("MACD(%d, %d, %d)", fastPeriod, slowPeriod, signalPeriod);
    }
}
