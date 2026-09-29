package com.quantback.strategy;

import com.quantback.data.Candle;
import com.quantback.data.MarketData;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Quantpedia #49: The Halloween Effect ("Sell in May and Go Away")
 * Based on Bouman and Jacobsen (2002): "The Halloween Indicator, 'Sell in May and Go Away': Another Puzzle",
 * American Economic Review.
 *
 * Rule:
 * - Stock returns are historically significantly higher during the winter months
 *   (November through April) than during the summer months (May through October).
 * - BUY when the current month is within the winter holding window (default: November 1st to April 30th).
 * - SELL (exit to cash) when the current month is outside this window (default: May 1st to October 31st).
 */
public class HalloweenEffectStrategy implements Strategy {

    private final int entryMonth;
    private final int exitMonth;

    public HalloweenEffectStrategy() {
        this(11, 5); // Default: Enter in November (11), exit in May (5)
    }

    public HalloweenEffectStrategy(int entryMonth, int exitMonth) {
        if (entryMonth < 1 || entryMonth > 12) {
            throw new IllegalArgumentException("Entry month must be between 1 and 12: " + entryMonth);
        }
        if (exitMonth < 1 || exitMonth > 12) {
            throw new IllegalArgumentException("Exit month must be between 1 and 12: " + exitMonth);
        }
        if (entryMonth == exitMonth) {
            throw new IllegalArgumentException("Entry month and exit month cannot be identical: " + entryMonth);
        }
        this.entryMonth = entryMonth;
        this.exitMonth = exitMonth;
    }

    public int getEntryMonth() {
        return entryMonth;
    }

    public int getExitMonth() {
        return exitMonth;
    }

    @Override
    public Signal generateSignal(MarketData historicalData) {
        Objects.requireNonNull(historicalData, "Historical data cannot be null");

        if (historicalData.isEmpty()) {
            return Signal.HOLD;
        }

        Candle currentCandle = historicalData.get(historicalData.size() - 1);
        LocalDate currentDate = currentCandle.getTimestamp().toLocalDate();
        int month = currentDate.getMonthValue();

        boolean inHoldingWindow;
        if (entryMonth > exitMonth) {
            // Winter holding window wrapping year-end, e.g. Nov (11) through Apr (4)
            inHoldingWindow = (month >= entryMonth || month < exitMonth);
        } else {
            // Regular range within the same calendar year, e.g. May (5) to Sep (9)
            inHoldingWindow = (month >= entryMonth && month < exitMonth);
        }

        return inHoldingWindow ? Signal.BUY : Signal.SELL;
    }

    @Override
    public String getName() {
        return String.format("HalloweenEffect(EntryMonth=%d, ExitMonth=%d)", entryMonth, exitMonth);
    }
}
