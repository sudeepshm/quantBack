package com.quantback.strategy.multi;

import com.quantback.data.MarketData;
import com.quantback.strategy.Signal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Quantpedia #50 & #51: Pairs Trading (Distance / Spread Mean Reversion)
 * Based on Gatev, Goetzmann, and Rouwenhorst (2006): "Pairs Trading: Performance of a
 * Relative-Value Arbitrage Rule".
 *
 * Rule:
 * - Tracks the normalized price ratio spread between two assets (Symbol A and Symbol B).
 * - Ratio R = Price_A / Price_B
 * - Z-Score = (R - Mean(R)) / StdDev(R)
 * - If Z >= entryZ (spread diverged upward): Asset A is relatively overvalued -> SELL A, BUY B.
 * - If Z <= -entryZ (spread diverged downward): Asset A is relatively undervalued -> BUY A, SELL B.
 * - If |Z| <= exitZ (spread converged to equilibrium): close positions / SELL both (exit to cash).
 */
public class PairsTradingStrategy implements MultiAssetStrategy {

    private final String symbolA;
    private final String symbolB;
    private final int lookbackBars;
    private final double entryZScore;
    private final double exitZScore;
    private static final int SCALE = 6;

    public PairsTradingStrategy(String symbolA, String symbolB, int lookbackBars, double entryZScore, double exitZScore) {
        Objects.requireNonNull(symbolA, "Symbol A cannot be null");
        Objects.requireNonNull(symbolB, "Symbol B cannot be null");
        if (symbolA.equalsIgnoreCase(symbolB)) {
            throw new IllegalArgumentException("Pairs trading requires two distinct symbols");
        }
        if (lookbackBars <= 2) {
            throw new IllegalArgumentException("Lookback bars must be greater than 2: " + lookbackBars);
        }
        if (entryZScore <= 0 || exitZScore < 0 || exitZScore >= entryZScore) {
            throw new IllegalArgumentException("Invalid Z-score parameters: exitZScore must be non-negative and less than entryZScore");
        }
        this.symbolA = symbolA.toUpperCase();
        this.symbolB = symbolB.toUpperCase();
        this.lookbackBars = lookbackBars;
        this.entryZScore = entryZScore;
        this.exitZScore = exitZScore;
    }

    public String getSymbolA() {
        return symbolA;
    }

    public String getSymbolB() {
        return symbolB;
    }

    public int getLookbackBars() {
        return lookbackBars;
    }

    public double getEntryZScore() {
        return entryZScore;
    }

    public double getExitZScore() {
        return exitZScore;
    }

    @Override
    public Map<String, Signal> generateSignals(Map<String, MarketData> universeData, LocalDateTime currentTimestamp) {
        Objects.requireNonNull(universeData, "Universe data cannot be null");

        Map<String, Signal> signals = new HashMap<>();
        signals.put(symbolA, Signal.HOLD);
        signals.put(symbolB, Signal.HOLD);

        MarketData dataA = universeData.get(symbolA);
        MarketData dataB = universeData.get(symbolB);

        if (dataA == null || dataB == null) {
            return signals;
        }

        int minSize = Math.min(dataA.size(), dataB.size());
        if (minSize < lookbackBars) {
            return signals;
        }

        // Compute ratio series over lookbackBars
        double[] ratios = new double[lookbackBars];
        double sum = 0;

        int offsetA = dataA.size() - lookbackBars;
        int offsetB = dataB.size() - lookbackBars;

        for (int i = 0; i < lookbackBars; i++) {
            double priceA = dataA.get(offsetA + i).getClose().doubleValue();
            double priceB = dataB.get(offsetB + i).getClose().doubleValue();
            double r = priceB > 0 ? priceA / priceB : 1.0;
            ratios[i] = r;
            sum += r;
        }

        double mean = sum / lookbackBars;
        double varianceSum = 0;
        for (double r : ratios) {
            varianceSum += Math.pow(r - mean, 2);
        }
        double stdDev = Math.sqrt(varianceSum / (lookbackBars - 1));

        if (stdDev < 1e-8) {
            return signals;
        }

        double currentRatio = ratios[lookbackBars - 1];
        double zScore = (currentRatio - mean) / stdDev;

        if (zScore >= entryZScore) {
            // A overvalued, B undervalued
            signals.put(symbolA, Signal.SELL);
            signals.put(symbolB, Signal.BUY);
        } else if (zScore <= -entryZScore) {
            // A undervalued, B overvalued
            signals.put(symbolA, Signal.BUY);
            signals.put(symbolB, Signal.SELL);
        } else if (Math.abs(zScore) <= exitZScore) {
            // Mean reversion reversion to equilibrium: take profit / unwind
            signals.put(symbolA, Signal.SELL);
            signals.put(symbolB, Signal.SELL);
        }

        return signals;
    }

    @Override
    public String getName() {
        return String.format("PairsTrading(%s/%s, %db, entry=%.1fσ, exit=%.1fσ)",
                symbolA, symbolB, lookbackBars, entryZScore, exitZScore);
    }
}
