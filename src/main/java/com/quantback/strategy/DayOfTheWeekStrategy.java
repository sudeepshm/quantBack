package com.quantback.strategy;

import com.quantback.data.Candle;
import com.quantback.data.MarketData;

import java.time.DayOfWeek;
import java.util.Objects;

/**
 * Quantpedia #16: Day-of-the-Week / Weekend Effect Anomaly
 * Based on French (1980): "Stock Returns and the Weekend Effect", Journal of Financial Economics
 * and Gibbons & Hess (1981): "Day of the Week Effects and Asset Returns", Journal of Business.
 *
 * Rule:
 * - Empirical research documents that average stock returns on Mondays are significantly lower
 *   (often negative) than on other days, with returns peaking mid-week to Friday.
 * - BUY and hold during favorable trading days (default: Tuesday through Friday).
 * - SELL (exit to cash) ahead of weekends and Monday trading (default: Monday or weekends).
 */
public class DayOfTheWeekStrategy implements Strategy {

    private final int entryDayOfWeek; // 1 = Monday, 7 = Sunday
    private final int exitDayOfWeek;  // 1 = Monday, 7 = Sunday

    public DayOfTheWeekStrategy() {
        this(DayOfWeek.TUESDAY.getValue(), DayOfWeek.FRIDAY.getValue());
    }

    public DayOfTheWeekStrategy(int entryDayOfWeek, int exitDayOfWeek) {
        if (entryDayOfWeek < 1 || entryDayOfWeek > 7) {
            throw new IllegalArgumentException("Entry day of week must be between 1 (Monday) and 7 (Sunday): " + entryDayOfWeek);
        }
        if (exitDayOfWeek < 1 || exitDayOfWeek > 7) {
            throw new IllegalArgumentException("Exit day of week must be between 1 (Monday) and 7 (Sunday): " + exitDayOfWeek);
        }
        if (entryDayOfWeek > exitDayOfWeek) {
            throw new IllegalArgumentException("Entry day of week cannot be greater than exit day: " + entryDayOfWeek + " > " + exitDayOfWeek);
        }
        this.entryDayOfWeek = entryDayOfWeek;
        this.exitDayOfWeek = exitDayOfWeek;
    }

    public int getEntryDayOfWeek() {
        return entryDayOfWeek;
    }

    public int getExitDayOfWeek() {
        return exitDayOfWeek;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        if (historicalData.isEmpty()) {
            return Signal.HOLD;
        }

        Candle currentCandle = historicalData.get(historicalData.size() - 1);
        int dayValue = currentCandle.getTimestamp().toLocalDate().getDayOfWeek().getValue();

        if (dayValue >= entryDayOfWeek && dayValue <= exitDayOfWeek) {
            return Signal.BUY;
        } else {
            return Signal.SELL;
        }
    }

    @Override
    public String getName() {
        return String.format("DayOfTheWeek(%s-%s)", DayOfWeek.of(entryDayOfWeek), DayOfWeek.of(exitDayOfWeek));
    }
}
