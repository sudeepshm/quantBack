package com.quantback.strategy;

import com.quantback.data.MarketData;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Quantpedia #28: Donchian Channel Breakout (Turtle Trading Strategy)
 * Based on Richard Donchian (1960) and Curtis Faith (2007) "The Way of the Turtle".
 *
 * Rules:
 * - Upper Channel (Entry): Highest high of the preceding N entry periods.
 * - Lower Channel (Exit): Lowest low of the preceding M exit periods.
 * - BUY when the current close price breaks above the Upper Channel (new N-period high).
 * - SELL when the current close price breaks below the Lower Exit Channel (new M-period low).
 * - HOLD when within channel bounds or historical data is insufficient.
 *
 * Strictly prevents look-ahead bias by evaluating historical highs/lows prior to current bar [t - N, t - 1].
 */
public class DonchianChannelStrategy implements Strategy {

    private final int entryPeriod;
    private final int exitPeriod;

    public DonchianChannelStrategy() {
        this(20, 10);
    }

    public DonchianChannelStrategy(int entryPeriod, int exitPeriod) {
        if (entryPeriod <= 1) {
            throw new IllegalArgumentException("Entry period must be greater than 1: " + entryPeriod);
        }
        if (exitPeriod < 1) {
            throw new IllegalArgumentException("Exit period must be at least 1: " + exitPeriod);
        }
        this.entryPeriod = entryPeriod;
        this.exitPeriod = exitPeriod;
    }

    public int getEntryPeriod() {
        return entryPeriod;
    }

    public int getExitPeriod() {
        return exitPeriod;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        int minRequired = Math.max(entryPeriod, exitPeriod) + 1;
        if (historicalData.size() < minRequired) {
            return Signal.HOLD;
        }

        int currentIdx = historicalData.size() - 1;

        // 1. Calculate Upper Channel (Highest High of previous entryPeriod bars)
        BigDecimal upperChannel = historicalData.get(currentIdx - 1).getHigh();
        for (int i = currentIdx - entryPeriod; i < currentIdx; i++) {
            BigDecimal high = historicalData.get(i).getHigh();
            if (high.compareTo(upperChannel) > 0) {
                upperChannel = high;
            }
        }

        // 2. Calculate Lower Exit Channel (Lowest Low of previous exitPeriod bars)
        BigDecimal lowerExitChannel = historicalData.get(currentIdx - 1).getLow();
        for (int i = currentIdx - exitPeriod; i < currentIdx; i++) {
            BigDecimal low = historicalData.get(i).getLow();
            if (low.compareTo(lowerExitChannel) < 0) {
                lowerExitChannel = low;
            }
        }

        BigDecimal currentClose = historicalData.get(currentIdx).getClose();

        // 3. Breakout signals
        if (currentClose.compareTo(upperChannel) > 0) {
            return Signal.BUY;
        } else if (currentClose.compareTo(lowerExitChannel) < 0) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    @Override
    public String getName() {
        return String.format("DonchianChannel(%d, %d)", entryPeriod, exitPeriod);
    }
}
