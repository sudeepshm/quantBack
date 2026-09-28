package com.quantback.strategy;

import com.quantback.data.MarketData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Quantpedia #2: 52-Weeks High Effect in Stocks
 * Based on George and Hwang (2004): "The 52-Week High and Momentum Investing".
 *
 * Rule:
 * - Computes the highest high over the past lookback period (default 252 bars / ~52 weeks).
 * - BUY when the current close price is near the high (within thresholdPercent of the highest high, e.g. 5%).
 * - SELL when the current close price drops below the exitBufferPercent (e.g. 8% below the high).
 * - HOLD otherwise or if there is insufficient historical data.
 */
public class FiftyTwoWeekHighStrategy implements Strategy {

    private final int lookbackPeriod;
    private final double thresholdPercent;
    private final double exitBufferPercent;
    private static final int SCALE = 6;

    public FiftyTwoWeekHighStrategy(int lookbackPeriod, double thresholdPercent, double exitBufferPercent) {
        if (lookbackPeriod <= 0) {
            throw new IllegalArgumentException("Lookback period must be positive: " + lookbackPeriod);
        }
        if (thresholdPercent < 0 || thresholdPercent >= 100) {
            throw new IllegalArgumentException("Threshold percent must be between 0 and 100: " + thresholdPercent);
        }
        if (exitBufferPercent <= thresholdPercent || exitBufferPercent >= 100) {
            throw new IllegalArgumentException("Exit buffer percent must be strictly greater than threshold percent and less than 100: " + exitBufferPercent);
        }
        this.lookbackPeriod = lookbackPeriod;
        this.thresholdPercent = thresholdPercent;
        this.exitBufferPercent = exitBufferPercent;
    }

    public int getLookbackPeriod() {
        return lookbackPeriod;
    }

    public double getThresholdPercent() {
        return thresholdPercent;
    }

    public double getExitBufferPercent() {
        return exitBufferPercent;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        // Need at least lookbackPeriod candles
        if (historicalData.size() < lookbackPeriod) {
            return Signal.HOLD;
        }

        int currentIdx = historicalData.size() - 1;
        BigDecimal currentClose = historicalData.get(currentIdx).getClose();

        // Calculate highest high over past lookbackPeriod (excluding current bar or including past window)
        int startIdx = currentIdx - lookbackPeriod;
        BigDecimal highestHigh = BigDecimal.ZERO;
        for (int i = startIdx; i < currentIdx; i++) {
            BigDecimal barHigh = historicalData.get(i).getHigh();
            if (barHigh.compareTo(highestHigh) > 0) {
                highestHigh = barHigh;
            }
        }

        if (highestHigh.compareTo(BigDecimal.ZERO) <= 0) {
            return Signal.HOLD;
        }

        BigDecimal buyLevel = highestHigh.multiply(
                BigDecimal.ONE.subtract(BigDecimal.valueOf(thresholdPercent / 100.0))
        );

        BigDecimal sellLevel = highestHigh.multiply(
                BigDecimal.ONE.subtract(BigDecimal.valueOf(exitBufferPercent / 100.0))
        );

        if (currentClose.compareTo(buyLevel) >= 0) {
            return Signal.BUY;
        } else if (currentClose.compareTo(sellLevel) < 0) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    @Override
    public String getName() {
        return String.format("52WeekHigh(%d, %.1f%%, %.1f%%)", lookbackPeriod, thresholdPercent, exitBufferPercent);
    }
}
