package com.quantback.strategy;

import com.quantback.data.MarketData;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Quantpedia #78: Turn of the Month in Equity Indexes
 * Based on Lakonishok and Smidt (1988): "Are Seasonal Anomalies Real? A Ninety-Year Perspective"
 * and Hensel and Ziemba (1996): "Investment Results from Exploiting Turn-of-the-Month Effects".
 *
 * Rule:
 * - Equity returns are systematically higher during the turn of the month (last few days of
 *   the current month and first few days of the next month).
 * - BUY when the current candle date is within the last `daysBeforeMonthEnd` days of the month
 *   OR the first `daysAfterMonthStart` days of the month.
 * - SELL when outside this window (move to cash).
 */
public class TurnOfTheMonthStrategy implements Strategy {

    private final int daysBeforeMonthEnd;
    private final int daysAfterMonthStart;

    public TurnOfTheMonthStrategy(int daysBeforeMonthEnd, int daysAfterMonthStart) {
        if (daysBeforeMonthEnd <= 0) {
            throw new IllegalArgumentException("Days before month end must be positive: " + daysBeforeMonthEnd);
        }
        if (daysAfterMonthStart <= 0) {
            throw new IllegalArgumentException("Days after month start must be positive: " + daysAfterMonthStart);
        }
        this.daysBeforeMonthEnd = daysBeforeMonthEnd;
        this.daysAfterMonthStart = daysAfterMonthStart;
    }

    public int getDaysBeforeMonthEnd() {
        return daysBeforeMonthEnd;
    }

    public int getDaysAfterMonthStart() {
        return daysAfterMonthStart;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        if (historicalData.isEmpty()) {
            return Signal.HOLD;
        }

        LocalDate currentDate = historicalData.get(historicalData.size() - 1).getTimestamp().toLocalDate();
        int dayOfMonth = currentDate.getDayOfMonth();
        int lengthOfMonth = currentDate.lengthOfMonth();

        boolean isStartOfMonth = dayOfMonth <= daysAfterMonthStart;
        boolean isEndOfMonth = dayOfMonth > (lengthOfMonth - daysBeforeMonthEnd);

        if (isStartOfMonth || isEndOfMonth) {
            return Signal.BUY;
        } else {
            return Signal.SELL;
        }
    }

    @Override
    public String getName() {
        return String.format("TurnOfTheMonth(-%dd, +%dd)", daysBeforeMonthEnd, daysAfterMonthStart);
    }
}
